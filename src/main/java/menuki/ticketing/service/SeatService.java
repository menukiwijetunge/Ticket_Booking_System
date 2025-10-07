package menuki.ticketing.service;

import menuki.ticketing.model.Seat;
import java.util.List;
import java.util.Map;

/**
 * Service layer contract for business logic related to seats
 */
public interface SeatService {
    List<Seat> loadSeatsForEvent(String eventId);
    boolean reserveSeats(String eventId, List<String> seatIds);
    void addHeldByOthers(String eventId, List<String> seatIds);
    void releaseSeats(String eventId, List<String> seatIds);
    Map<String, List<Seat>> loadSeatsGroupedByRow(String eventId);
    void markReserved(List<Seat> seats);
    void markAvailable(List<Seat> seats);
    void markVipSeats(String eventId, List<String> vipRows, int vipCents);
    void markStandardSeats(String eventId, List<String> vipRows, int stdCents);
}
