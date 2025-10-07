package menuki.ticketing.service;

import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.data.jdbc.JdbcSeatDao;
import menuki.ticketing.model.Seat;
import menuki.ticketing.model.SeatStatus;
import menuki.ticketing.model.SeatType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * Service layer acting as the bridge between DAOs and the rest of the application
 * Handles business logic for seats
 */
public class JdbcSeatService implements SeatService {


    private final SeatDao seatDao;

    //thread-safe flag ensuring DB table us only created once
    private static volatile boolean tableChecked = false;

    //Constructors

    //Normal one used by the app
    public JdbcSeatService() {
        this(new JdbcSeatDao());
    }

    // For testing - inject any DAO implementation
    public JdbcSeatService(SeatDao seatDao) {
        this.seatDao = seatDao;
    }

    /*
     * Business logic for ensuring seat availability for a certain event
     */
    @Override
    public List<Seat> loadSeatsForEvent(String eventId) {
        try {
            // Done for performance by avoiding unwanted table creation
            if (!tableChecked) {
                synchronized (JdbcSeatService.class) {
                    if (!tableChecked) {
                        seatDao.createTableIfNotExists();
                        tableChecked = true;
                        System.out.println("[SERVICE] seats table ensured once");
                    }
                }
            }
            // Check if there are seats stored for this event and if not, generate the default layout
            if (!seatDao.hasAnyForEvent(eventId)) {
                List<Seat> all = generateDefaultLayout(eventId);
                long t0 = System.currentTimeMillis();
                seatDao.insertAll(eventId, all);
            }

            return seatDao.findByEvent(eventId);
        } catch (SQLException e) {
            throw new RuntimeException("loadSeatsForEvent failed", e);
        }
    }


   /*
    * Business logic for reserving seats
    */
    @Override
    public boolean reserveSeats(String eventId, List<String> seatIds) {
        try {
            return seatDao.reserveSeatsAtomic(eventId, seatIds);
        } catch (SQLException e) {
            throw new RuntimeException("reserveSeats failed", e);
        }
    }

    /*
     * Business logic for identifying seats held by others
     */
    @Override
    public void addHeldByOthers(String eventId, List<String> seatIds) {
        try {
            seatDao.markReserved(eventId, seatIds);
        } catch (SQLException e) {
            throw new RuntimeException("addHeldByOthers failed", e);
        }
    }

    /*
     * Business logic for generating the default seating arrangement
     */
    private List<Seat> generateDefaultLayout(String eventId) {
        List<Seat> out = new ArrayList<>();

        int[] rowWidths = {10, 12, 14, 16, 18, 20, 22, 22, 22, 20, 18, 16, 14, 12};
        int vipRowIndex = 7; //roughly center

        final int STANDARD_PRICE = 1200; // $12.00 AUD
        final int VIP_PRICE = 2500;      // $25.00 AUD

        for (int r = 0; r < rowWidths.length; r++) {
            int width = rowWidths[r];
            String rowLabel = String.valueOf((char) ('A' + r));

            for (int c = 1; c <= width; c++) {
                SeatType type = (r == vipRowIndex) ? SeatType.VIP : SeatType.STANDARD;
                int price = (type == SeatType.VIP) ? VIP_PRICE : STANDARD_PRICE;

                out.add(new Seat(eventId, rowLabel, c, type, SeatStatus.AVAILABLE, price));
            }
        }
        return out;
    }


    /*
     * Business logic for making seats available again (releasing)
     */
    @Override
    public void releaseSeats(String eventId, List<String> seatIds) {
        try { seatDao.markAvailable(eventId, seatIds); }
        catch (SQLException e) { throw new RuntimeException("releaseSeats failed", e); }
    }



    /*
     * Business Logic for getting all seats for an event grouped by the row label (ready for UI)
     */
    @Override
    public Map<String, List<Seat>> loadSeatsGroupedByRow(String eventId) {
        List<Seat> seats = loadSeatsForEvent(eventId);
        return seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowLabel, TreeMap::new, Collectors.toList()));
    }

    /*
     * Business logic for marking seats as reserved
     */
    @Override
    public void markReserved(List<Seat> seats) {
        seats.forEach(s -> s.setStatus(SeatStatus.RESERVED));
    }

    /*
     * Business logic for marking seats as available
     */
    @Override
    public void markAvailable(List<Seat> seats) {
        seats.forEach(s -> s.setStatus(SeatStatus.AVAILABLE));
    }


    /*
     * Business logic for marking which seats are VIP
     */
    @Override
    public void markVipSeats(String eventId, List<String> vipRows, int vipCents) {
        try {
            seatDao.setVipSeats(eventId, vipRows, vipCents);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set VIP seats", e);
        }
    }

    /*
     * Business logic for marking which seats are STANDARD
     */
    @Override
    public void markStandardSeats(String eventId, List<String> vipRows, int stdCents) {
        try {
            seatDao.setStandardSeats(eventId, vipRows, stdCents);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set standard seats", e);
        }
    }


}
