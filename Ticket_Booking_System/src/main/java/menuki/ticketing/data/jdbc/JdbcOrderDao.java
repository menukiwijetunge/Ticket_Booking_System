package menuki.ticketing.data.jdbc;

import menuki.ticketing.data.dao.OrderDao;
import menuki.ticketing.model.Order;
import menuki.ticketing.model.Seat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Implements OrderDao and manages communication with database using JDBC for all order related info
 */
public class JdbcOrderDao implements OrderDao {

    /*
     * Creates the Orders table if it does not exist
     */
    @Override
    public void createTableIfNotExists() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id VARCHAR(50) NOT NULL,
                    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    total_cents INT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(username)
                        ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("createTableIfNotExists(orders) failed", e);
        }
    }

    /*
     * This is used to Create an order/ Insert an order to teh orders table
     */
    @Override
    public int createOrder(String userId, String eventId, List<Seat> seats) throws SQLException {
        Objects.requireNonNull(userId, "userId");
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("No seats provided");
        }
        //Using java streams to calculate total
        int totalCents = seats.stream().mapToInt(Seat::getPriceCents).sum();
        final String insertOrderSql =
                "INSERT INTO orders (user_id, total_cents) VALUES (?, ?)";

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            boolean origAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                int orderId;

                //Insert into orders table
                try (PreparedStatement ps = c.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, userId);
                    ps.setInt(2, totalCents);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Failed to obtain generated order id");
                        orderId = rs.getInt(1);
                    }
                }

                //insert order_items using the same connection
                JdbcOrderItemDao orderItemDao = new JdbcOrderItemDao();
                orderItemDao.insertItems(c, orderId, seats);

                c.commit();
                return orderId;
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException se) throw se;
                throw new SQLException("createOrder failed", ex);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Failed to get or use DB connection", e);
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignore) {}
        }
    }


    /*
     * Find orders for a certain user
     */
    @Override
    public List<Order> findByUser(String userId) throws SQLException {
        String sql = "SELECT id, user_id, booked_at FROM orders WHERE user_id = ? ORDER BY booked_at DESC";
        //Store orders result from query in a List
        List<Order> orders = new ArrayList<>();

        Connection conn = null;
        try {

            conn = DatabaseConnection.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setUserId(rs.getString("user_id"));
                        order.setCreatedAt(rs.getTimestamp("booked_at").toLocalDateTime());
                        orders.add(order);
                    }
                }
            }

            return orders;

        } catch (Exception e) {
            throw new SQLException("Error retrieving orders for user: " + userId, e);
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    /*
     *Count order items per event
     */
    @Override
    public int countItemsForEvent(String eventId) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM order_items WHERE event_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("countItemsForEvent failed", e);
        }
    }

    /*
     * Used to delete orderItems by event
     */
    @Override
    public int deleteOrderItemsByEvent(String eventId) throws SQLException {
        final String sql = "DELETE FROM order_items WHERE event_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, eventId);
            return ps.executeUpdate();
        } catch (Exception e) {
            if (e instanceof SQLException se) throw se;
            throw new SQLException("deleteOrderItemsByEvent failed", e);
        }
    }


}














