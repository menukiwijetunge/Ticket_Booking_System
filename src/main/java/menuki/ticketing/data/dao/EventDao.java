package menuki.ticketing.data.dao;

import menuki.ticketing.model.Event;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object used for Event entity. Abstraction layer for event-related DB operations
 */
public interface EventDao {

    void createTableIfNotExists() throws SQLException;

    void insert(Event event) throws SQLException;

    Event findById(String id) throws SQLException;

    List<Event> findAll() throws SQLException;

    boolean existsById(String id) throws SQLException;

    boolean deleteById(String id) throws java.sql.SQLException;

    Map<String, Integer> countAvailableByEvent() throws SQLException;

    String getNextEventId() throws SQLException;
}
