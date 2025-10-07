package menuki.ticketing.service;

import menuki.ticketing.model.Order;
import menuki.ticketing.model.Seat;

import java.util.List;

/**
 * Service layer contract for business logic related to orders
 */
public interface OrderService {

    int createOrder(String userId, String eventId, List<Seat> seats);
    List<Order> findByUser(String username);
}
