package menuki.ticketing.data.dao;

import menuki.ticketing.model.User;
import java.sql.SQLException;
import java.util.List;


/**
 * Data Access Object used for User entity. Abstraction layer for user-related DB operations.
 */
public interface UserDao {

    User findByUsername(String username);

    List<User> findAll();

    void createTableIfNotExists() throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    void insert(User user) throws SQLException;
}
