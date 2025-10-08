package menuki.ticketing.data.dao;

import menuki.ticketing.model.OrderItem;
import menuki.ticketing.model.Seat;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


/**
 * Data Access Object used for OrderItem entity. Abstraction layer for order item-related DB operations.
 */
public interface OrderItemDao {
    void createTableIfNotExists() throws SQLException;

    void insertItems(Connection c, int orderId, List<Seat> seats) throws SQLException;

    List<OrderItem> findByOrderId(int orderId) throws SQLException;
}
