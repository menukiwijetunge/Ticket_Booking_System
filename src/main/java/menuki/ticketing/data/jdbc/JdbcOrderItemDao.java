package menuki.ticketing.data.jdbc;

import menuki.ticketing.data.dao.OrderItemDao;
import menuki.ticketing.model.OrderItem;
import menuki.ticketing.model.Seat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Implements OrderItemDao and manages communication with database using JDBC for all order item related info
 */
public class JdbcOrderItemDao implements OrderItemDao {


    /*
     * Creates the order_items table if it does not exist
     */
    @Override
    public void createTableIfNotExists() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                  id           INT AUTO_INCREMENT PRIMARY KEY,
                  order_id     INT         NOT NULL,
                  event_id     VARCHAR(64) NOT NULL,
                  row_label    VARCHAR(4)  NOT NULL,
                  seat_number  INT         NOT NULL,
                  price_cents  INT         NOT NULL,
                  FOREIGN KEY (order_id) REFERENCES orders(id)
                    ON DELETE CASCADE ON UPDATE CASCADE,
                  FOREIGN KEY (event_id, row_label, seat_number)
                    REFERENCES seats(event_id, row_label, seat_number)
                    ON DELETE RESTRICT ON UPDATE CASCADE
                )
            """);
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("createTableIfNotExists(order_items) failed", e);
        }
    }

    /*
     * Used to insert items to the order_items table
     */
    @Override
    public void insertItems(Connection c, int orderId, List<Seat> seats) throws SQLException {
        //If no seats, nothing to add to table
        if (seats == null || seats.isEmpty()) return;

        final String sql =
                "INSERT INTO order_items (order_id, event_id, row_label, seat_number, price_cents) " +
                        "VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Seat s : seats) {
                ps.setInt(1, orderId);
                ps.setString(2, s.getEventId());
                ps.setString(3, s.getRowLabel());
                ps.setInt(4, s.getSeat_number());
                ps.setInt(5, s.getPriceCents());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }


    /*
     * Find order_items by order
     */
    @Override
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        List<OrderItem> out = new ArrayList<>();
        final String sql = "SELECT id, order_id, event_id, row_label, seat_number, price_cents FROM order_items WHERE order_id=?";


        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new OrderItem(
                                rs.getInt("id"),
                                rs.getInt("order_id"),
                                rs.getString("event_id"),
                                rs.getString("row_label"),
                                rs.getInt("seat_number"),
                                rs.getInt("price_cents")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("findByOrderId failed", e);
        } finally {
            if (c != null) try {
                c.close();
            } catch (SQLException ignore) {
            }
        }

        return out;
    }

}

