package menuki.ticketing.data.dao;

import menuki.ticketing.model.Order;
import menuki.ticketing.model.Seat;
import java.sql.SQLException;
import java.util.List;


/**
 * Data Access Object used for Order entity. Abstraction layer for order-related DB operations
 */
public interface OrderDao {

    void createTableIfNotExists() throws SQLException;

    int createOrder(String userId, String eventId, List<Seat> seats) throws SQLException;

    List<Order> findByUser(String username) throws java.sql.SQLException;

    int countItemsForEvent(String eventId) throws java.sql.SQLException;

    int deleteOrderItemsByEvent(String eventId) throws java.sql.SQLException;


}
