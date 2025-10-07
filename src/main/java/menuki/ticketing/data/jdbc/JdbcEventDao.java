package menuki.ticketing.data.jdbc;

import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.model.Event;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements EventDao and manages communication with database using JDBC for all event related info
 */
public class JdbcEventDao implements EventDao {

    /*
     * Creates the Events table if it does not exist
     */
    @Override
    public void createTableIfNotExists() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {

            st.execute("""
            CREATE TABLE IF NOT EXISTS events (
              id    VARCHAR(64) PRIMARY KEY,
              name  VARCHAR(255) NOT NULL,
              date  DATE NULL,
              venue VARCHAR(255),
              -- ADD THESE TWO NEW COLUMNS ↓↓↓
              start_time TIME NULL,
              end_time   TIME NULL
            )
        """);
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("createTableIfNotExists(events) failed", e);
        }
    }




    /*
     * Used to insert events into the events table
     */
    @Override
    public void insert(Event event) throws SQLException {
        final String sql = "INSERT INTO events(id, name, date, venue, start_time, end_time) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             //Using prepared statement to execute queries using user supplied values
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, event.getId());
            ps.setString(2, event.getName());
            if (event.getDate() != null) ps.setDate(3, Date.valueOf(event.getDate()));
            else ps.setNull(3, Types.DATE);
            ps.setString(4, event.getVenue());
            if (event.getStartTime() != null) ps.setTime(5, Time.valueOf(event.getStartTime()));
            else ps.setNull(5, Types.TIME);
            if (event.getEndTime() != null) ps.setTime(6, Time.valueOf(event.getEndTime()));
            else ps.setNull(6, Types.TIME);

            ps.executeUpdate();
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("insert(event) failed", e);
        }
    }

    /*
     * Used to find an event by its event ID
     */
    @Override
    public Event findById(String id) throws SQLException {
        final String sql = "SELECT id, name, date, venue, start_time, end_time FROM events WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                LocalDate date = (rs.getDate("date") != null) ? rs.getDate("date").toLocalDate() : null;
                LocalTime start = (rs.getTime("start_time") != null) ? rs.getTime("start_time").toLocalTime() : null;
                LocalTime end = (rs.getTime("end_time") != null) ? rs.getTime("end_time").toLocalTime() : null;
                return new Event(rs.getString("id"), rs.getString("name"), date, rs.getString("venue"), start, end, 0);
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("findById(event) failed", e);
        }
    }

    /*
     * Used to retrieve all events
     */
    @Override
    public List<Event> findAll() throws SQLException {

        final String sql = """
        SELECT id, name, date, venue, start_time, end_time
        FROM events
        ORDER BY (date IS NULL), date, name
        """;
        //Store the query result in an List of Event Objects
        List<Event> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate date = (rs.getDate("date") != null) ? rs.getDate("date").toLocalDate() : null;
                LocalTime start = (rs.getTime("start_time") != null) ? rs.getTime("start_time").toLocalTime() : null;
                LocalTime end = (rs.getTime("end_time") != null) ? rs.getTime("end_time").toLocalTime() : null;
                out.add(new Event(rs.getString("id"), rs.getString("name"), date, rs.getString("venue"), start, end, 0));
            }
            return out;
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("findAll(events) failed", e);
        }
    }

    /*
     * Checks for the existence of an event with a specific ID
     */
    @Override
    public boolean existsById(String id) throws SQLException {
        final String sql = "SELECT 1 FROM events WHERE id = ? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("existsById(event) failed", e);
        }
    }


    /*
     * Used to delete a certain event by its ID
     */
    @Override
    public boolean deleteById(String id) throws SQLException {
        final String sql = "DELETE FROM events WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("deleteById(event) failed", e);
        }
    }


    /*
     * Counts the number of seats per event
     */
    @Override
    public Map<String, Integer> countAvailableByEvent() throws SQLException {
        /* Query explanation:
         * Start with events table (FROM events e). LEFT JOIN seats table so match rows by event_id in both tables.
         * LEFT JOIN gives a result even if there are no seats (available = 0).
         * If AVAILABLE have a temp value of 1 else 0.
         * Add up all the 1's and 0's for each event. COALESCE ensures that if an event has no seats, when SUM returns NULL, replace that with 0.
         */
        final String sql = """
        SELECT e.id AS event_id,
               COALESCE(SUM(CASE WHEN s.status='AVAILABLE' THEN 1 ELSE 0 END), 0) AS available
        FROM events e
        LEFT JOIN seats s ON s.event_id = e.id
        GROUP BY e.id
        """;

        Map<String, Integer> out = new java.util.HashMap<>();
        try (var c = DatabaseConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                out.put(rs.getString("event_id"), rs.getInt("available"));
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("countAvailableByEvent failed", e);
        }
        return out;
    }



    /*
     * Generates an ID for an event to act as a unique identifier
     */
    @Override
    public String getNextEventId() throws SQLException {
        final String sql =
                "SELECT MAX(CAST(SUBSTRING_INDEX(id, '-', -1) AS UNSIGNED)) FROM events";

        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int next = 2001; // Starting point if table is empty
            if (rs.next()) {
                int maxSuffix = rs.getInt(1); // 0 if null
                if (maxSuffix >= 2001) next = maxSuffix + 1;
            }
            return "E-" + next;

        } catch (Exception e) {
            throw new SQLException("Failed to fetch next event id", e);
        }
    }



}
