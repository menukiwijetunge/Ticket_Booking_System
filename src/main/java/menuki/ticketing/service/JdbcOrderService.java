package menuki.ticketing.service;

import menuki.ticketing.data.dao.OrderDao;
import menuki.ticketing.model.Order;
import menuki.ticketing.model.Seat;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer acting as the bridge between DAOs and the rest of the application
 * Handles business logic for orders
 */
public class JdbcOrderService implements OrderService {

    private final OrderDao orderDao;

    //Constructor
    public JdbcOrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /*
     * Business logic to create an order
     */
    @Override
    public int createOrder(String username, String eventId, List<Seat> seats) {
        try {
            return orderDao.createOrder(username, eventId, seats);
        } catch (SQLException e) {
            throw new RuntimeException("Order creation failed", e);
        }
    }

    /*
     * Business logic to find orders by user
     */
    @Override
    public List<Order> findByUser(String username) {
        try {
            return orderDao.findByUser(username);
        } catch (SQLException e) {
            throw new RuntimeException("findByUser failed", e);
        }
    }
}
