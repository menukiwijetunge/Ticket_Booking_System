package menuki.ticketing.data.dao;

import menuki.ticketing.model.Seat;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


/**
 * Data Access Object used for Seat entity. Abstraction layer for seat-related DB operations.
 */
public interface SeatDao {

    void createTableIfNotExists() throws SQLException;

    boolean hasAnyForEvent(String eventId) throws SQLException;

    List<Seat> findByEvent(String eventId) throws SQLException;

    boolean reserveSeatsAtomic(String eventId, List<String> seatIds) throws SQLException;

    void markReserved(String eventId, List<String> seatIds) throws SQLException;

    void insertAll(String eventId, List<Seat> seats) throws SQLException;

    void markAvailable(String eventId, List<String> seatIds) throws SQLException;

    void createGrid(String eventId, String rows, int seatsPerRow) throws SQLException;

    void setVipSeats(String eventId, List<String> vipRows, int priceCents) throws SQLException;

    void setStandardSeats(String eventId, List<String> excludedVipRows, int priceCents) throws SQLException;

    Map<String, Integer> countAvailableByEvent() throws SQLException;
}
