package menuki.ticketing.data.jdbc;

import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.model.Seat;
import menuki.ticketing.model.SeatStatus;
import menuki.ticketing.model.SeatType;
import java.sql.*;
import java.util.*;


/**
 * Implements SeatDao and manages communication with database using JDBC for all seat related info
 */
public class JdbcSeatDao implements SeatDao {

    /*
     * Creates the Orders table if it does not exist
     */
    @Override
    public void createTableIfNotExists() throws SQLException {
        final String sql = """
        CREATE TABLE IF NOT EXISTS seats (
          event_id    VARCHAR(64)  NOT NULL,
          row_label   VARCHAR(4)   NOT NULL,
          seat_number INT          NOT NULL,
          type        ENUM('STANDARD','VIP') NOT NULL,
          status      ENUM('AVAILABLE','SELECTED','RESERVED') NOT NULL DEFAULT 'AVAILABLE',
          price_cents INT NOT NULL,
          PRIMARY KEY (event_id, row_label, seat_number),
          FOREIGN KEY (event_id) REFERENCES events(id)
            ON DELETE CASCADE ON UPDATE CASCADE
        )
        """;

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (Statement st = c.createStatement()) {
                st.execute(sql);
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("createTableIfNotExists(seats) failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


    /*
     * Checks if there is at leats one seat for that specific event
     */
    @Override
    public boolean hasAnyForEvent(String eventId) throws SQLException {
        final String sql = "SELECT 1 FROM seats WHERE event_id = ? LIMIT 1";
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("hasAnyForEvent failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


    /*
     * Get seats by event
     */
    @Override
    public List<Seat> findByEvent(String eventId) throws SQLException {
        final String sql = """
            SELECT row_label, seat_number, type, status, price_cents
            FROM seats
            WHERE event_id = ?
            ORDER BY row_label, seat_number
            """;
        // Store query results for seats in a List
        List<Seat> out = new ArrayList<>();

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new Seat(
                                eventId,
                                rs.getString("row_label"),
                                rs.getInt("seat_number"),
                                SeatType.valueOf(rs.getString("type").toUpperCase()),
                                SeatStatus.valueOf(rs.getString("status").toUpperCase()),
                                rs.getInt("price_cents")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("findByEvent failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
        return out;
    }

    /*
     * Used to reserve seats
     */
    @Override
    public boolean reserveSeatsAtomic(String eventId, List<String> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return true;

        final String update = """
        UPDATE seats
           SET status = 'RESERVED'
         WHERE event_id = ?
           AND row_label = ?
           AND seat_number = ?
           AND status = 'AVAILABLE'
        """;

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);
            int affected = 0;
            try (PreparedStatement ps = c.prepareStatement(update)) {
                for (String id : seatIds) {
                    ps.setString(1, eventId);
                    ps.setString(2, parseRowLabel(id));
                    ps.setInt(3, parseSeatNumber(id));
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                for (int n : counts) affected += (n > 0 ? 1 : 0);
            }
            if (affected != seatIds.size()) { c.rollback(); return false; }
            c.commit();
            return true;
        } catch (Exception e) {
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
            if (e instanceof SQLException se) throw se;
            throw new SQLException("reserveSeatsAtomic failed", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignore) {}
                try { c.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /*
     * Mark seat status as reserved
     */
    @Override
    public void markReserved(String eventId, List<String> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return;
        final String update = """
            UPDATE seats
               SET status = 'RESERVED'
             WHERE event_id = ?
               AND row_label = ?
               AND seat_number = ?
            """;


        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(update)) {
                for (String id : seatIds) {
                    ps.setString(1, eventId);
                    ps.setString(2, parseRowLabel(id));
                    ps.setInt(3, parseSeatNumber(id));
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("markReserved failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }



    /*
     * Use to insert all seats for that event
     */
    @Override
    public void insertAll(String eventId, List<Seat> seats) throws SQLException {
        if (seats == null || seats.isEmpty()) return;

        final String insert = """
        INSERT INTO seats(event_id,row_label,seat_number,type,status,price_cents)
        VALUES (?,?,?,?,?,?)
        """;
        Connection c = null;
        long T0 = System.currentTimeMillis();
        try {

            c = DatabaseConnection.getConnection();
            System.out.println("[DAO.insertAll] Using connection URL: " + c.getMetaData().getURL());
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                for (Seat s : seats) {
                    ps.setString(1, eventId);
                    ps.setString(2, s.getRowLabel());
                    ps.setInt(3, s.getSeat_number());
                    ps.setString(4, s.getType().name());
                    ps.setString(5, s.getStatus().name());
                    ps.setInt(6, s.getPriceCents());
                    ps.addBatch();
                }
                //Executing in batches to speed up
                System.out.println("[DAO.insertAll] batching " + seats.size() + " seats...");
                int[] counts = ps.executeBatch();
                long tBatch = System.currentTimeMillis();
                System.out.println("[DAO.insertAll] executeBatch took " + (tBatch - T0) + " ms, rows=" + counts.length);
            }
            c.commit();
            long tCommit = System.currentTimeMillis();
            System.out.println("[DAO.insertAll] commit took " + (tCommit - T0) + " ms (since start)");
        } catch (Exception e) {
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
            if (e instanceof SQLException se) throw se;
            throw new SQLException("insertAll failed", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignore) {}
                try { c.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /*
     * Change seat status to AVAILABLE
     */
    @Override
    public void markAvailable(String eventId, List<String> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return;

        final String update = """
        UPDATE seats
           SET status = 'AVAILABLE'
         WHERE event_id = ?
           AND row_label = ?
           AND seat_number = ?
        """;

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection(); // was: try (Connection c = conn(); ...
            try (PreparedStatement ps = c.prepareStatement(update)) {
                for (String id : seatIds) {
                    ps.setString(1, eventId);
                    ps.setString(2, parseRowLabel(id));
                    ps.setInt(3, parseSeatNumber(id));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("markAvailable failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }

    /*
     * Used to extract the row from the display ID
     */
    private static String parseRowLabel(String displayId) {
        int dash = displayId.indexOf('-');
        return (dash > 0) ? displayId.substring(0, dash) : displayId;
    }
    /*
     * Used to extract seat number from display ID
     */
    private static int parseSeatNumber(String displayId) {
        int dash = displayId.indexOf('-');
        if (dash >= 0 && dash + 1 < displayId.length()) {
            try { return Integer.parseInt(displayId.substring(dash + 1)); }
            catch (NumberFormatException ignore) {}
        }
        //In case it is not formatted as expected
        return Integer.parseInt(displayId.replaceAll("\\D+", ""));
    }


    /*
     * Use to set which rows will be VIP rows
     */
    @Override
    public void setVipSeats(String eventId, List<String> vipRows, int priceCents) throws SQLException {
        if (vipRows == null || vipRows.isEmpty()) return;
        String placeholders = String.join(",", Collections.nCopies(vipRows.size(), "?"));
        String sql = "UPDATE seats SET price_cents=?, type='VIP' " +
                "WHERE event_id=? AND row_label IN (" + placeholders + ")";

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                int i = 1;
                ps.setInt(i++, priceCents);
                ps.setString(i++, eventId);
                for (String r : vipRows) ps.setString(i++, r);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("setVipSeats failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


    /*
     * Use to set which rows will be STANDARD rows
     */
    @Override
    public void setStandardSeats(String eventId, List<String> excludedVipRows, int priceCents) throws SQLException {
        String sql = "UPDATE seats SET price_cents=?, type='STANDARD' WHERE event_id=?";
        if (excludedVipRows != null && !excludedVipRows.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(excludedVipRows.size(), "?"));
            sql += " AND row_label NOT IN (" + placeholders + ")";
        }

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                int i = 1;
                ps.setInt(i++, priceCents);
                ps.setString(i++, eventId);
                if (excludedVipRows != null) {
                    for (String r : excludedVipRows) ps.setString(i++, r);
                }
                ps.executeUpdate();
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("setStandardSeats failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


    /*
     * Generates seats for an event and inserts them to the seats table
     */
    @Override
    public void createGrid(String eventId, String rows, int seatsPerRow) throws SQLException {
        String[] rowLabels = rows.split("");

        //Initially all seats are available, STANDARD and cost 0
        final String sql =
                "INSERT INTO seats (event_id, row_label, seat_number, status, type, price_cents) " +
                        "VALUES (?, ?, ?, 'AVAILABLE', 'STANDARD', 0)";

        java.sql.Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String row : rowLabels) {
                    String r = row.trim();
                    if (r.isEmpty()) continue;

                    for (int i = 1; i <= seatsPerRow; i++) {
                        ps.setString(1, eventId);
                        ps.setString(2, r);
                        ps.setInt(3, i);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            c.commit();

        } catch (Exception e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ignore) {}
            }
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("createGrid failed", e);

        } finally {
            if (c != null) {
                try { c.close(); } catch (SQLException ignore) {}
            }
        }
    }


    /*
     * Used to get the number of seats available for a specific event
     */
    @Override
    public Map<String, Integer> countAvailableByEvent() throws SQLException {
        final String sql = """
            SELECT event_id,
                   SUM(CASE WHEN status='AVAILABLE' THEN 1 ELSE 0 END) AS available
            FROM seats
            GROUP BY event_id
        """;
        Map<String, Integer> out = new HashMap<>();

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("event_id"), rs.getInt("available"));
                }
            }
            return out;
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("countAvailableByEvent failed", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


}
