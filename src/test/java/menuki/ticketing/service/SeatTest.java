package menuki.ticketing.service;

import menuki.ticketing.model.Seat;
import menuki.ticketing.model.SeatStatus;
import menuki.ticketing.model.SeatType;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JdbcSeatService.
 * These tests focus on pure business logic.
 */
class SeatServiceTest {

    private final JdbcSeatService service = new JdbcSeatService();

    @Test /* This test checks if marking seats as reserved actually changes the seat object's status accordingly */
    void markReserved_changesSeatStatuses() {
        //Create dummy seat objects
        Seat s1 = new Seat("E1", "A", 1, SeatType.STANDARD, SeatStatus.AVAILABLE, 1200);
        Seat s2 = new Seat("E1", "A", 2, SeatType.VIP, SeatStatus.AVAILABLE, 2500);

        service.markReserved(List.of(s1, s2));
        //Check the status update
        assertEquals(SeatStatus.RESERVED, s1.getStatus());
        assertEquals(SeatStatus.RESERVED, s2.getStatus());
    }

    @Test /* This test checks if marking seats as available actually changes the seat object's status accordingly */
    void markAvailable_changesSeatStatusesBack() {
        //Create dummy seat objects
        Seat s1 = new Seat("E1", "B", 1, SeatType.STANDARD, SeatStatus.RESERVED, 1200);
        Seat s2 = new Seat("E1", "B", 2, SeatType.VIP, SeatStatus.RESERVED, 2500);

        service.markAvailable(List.of(s1, s2));
        //Check the status update
        assertEquals(SeatStatus.AVAILABLE, s1.getStatus());
        assertEquals(SeatStatus.AVAILABLE, s2.getStatus());
    }


    @Test /* This test checks that the default seating arrangement and layout is created as expected */
    void generateDefaultLayout_createsReasonableNumberOfSeats() throws Exception {
        //Giving class object representing JDBC seat service at runtime and get the generateDefaultLayout method
        Method method = JdbcSeatService.class.getDeclaredMethod("generateDefaultLayout", String.class);
        //Even though this method is private, should be able to access it
        method.setAccessible(true);


        // Calls this private method
        Object result = method.invoke(service, "E1");
        List<Seat> layout = (result instanceof List) ? (List<Seat>) result : new ArrayList<>();

        assertNotNull(layout);
        assertTrue(layout.size() > 100, "should generate over 100 seats");
        assertTrue(layout.stream().anyMatch(s -> s.getType() == SeatType.VIP), "should include VIP seats");
        assertTrue(layout.stream().anyMatch(s -> s.getType() == SeatType.STANDARD), "should include STANDARD seats");
    }


    @Test /* This test ensures that there is no crash or no exception is thrown when the list is empty*/
    void markReserved_handlesEmptyListGracefully() {
        assertDoesNotThrow(() -> service.markReserved(List.of()));
    }

}
