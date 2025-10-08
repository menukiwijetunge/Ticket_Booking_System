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
     * This is used to check if a username already exists in the database
     */
    public boolean existsByUsername(String username) throws SQLException {
        final String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("existsByUsername failed", e);
        }
    }

    /*
     * This method is used to insert a new user
     */
    public void insert(User user) throws SQLException {
        final String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("insert(user) failed", e);
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

