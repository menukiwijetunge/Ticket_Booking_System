package menuki.ticketing.data.jdbc;

import menuki.ticketing.data.dao.UserDao;
import menuki.ticketing.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements UserDao and manages communication with database using JDBC for all user related info
 */
public class JdbcUserDao implements UserDao {

    /*
     * Creates the Orders table if it does not exist
     */
    @Override
    public void createTableIfNotExists() throws SQLException {
        final String sql = """
        CREATE TABLE IF NOT EXISTS users (
          username VARCHAR(50) PRIMARY KEY,
          password VARCHAR(255) NOT NULL,
          role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER'
        );
        """;

        try (Connection c = DatabaseConnection.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("createTableIfNotExists(users) failed", e);
        }
    }


    /*
     * Inserting default users
     */
    @Override
    public void insertDefaultUsers() throws SQLException {
        final String sql = """
        INSERT INTO users (username, password, role)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE username = username
    """;

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, "ADMIN");
                ps.addBatch();

                ps.setString(1, "user1");
                ps.setString(2, "password");
                ps.setString(3, "USER");
                ps.addBatch();

                ps.executeBatch();
            }

            c.commit();
        } catch (Exception e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ignore) {}
            }
            if (e instanceof SQLException se) throw se;
            throw new SQLException("insertDefaultUsers failed", e);
        } finally {
            if (c != null) {
                try { c.close(); } catch (SQLException ignore) {}
            }
        }
    }




    /*
     * Finding users by username
     */
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT username, password, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
        return null;
    }

    /*
     * Get all users ordered by username
     */
    @Override
    public List<User> findAll() {
        String sql = "SELECT username, password, role FROM users ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }
        return users;
    }
}

