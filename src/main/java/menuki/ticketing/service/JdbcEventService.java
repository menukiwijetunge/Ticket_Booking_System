package menuki.ticketing.service;

import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.data.jdbc.DatabaseConnection;
import menuki.ticketing.data.jdbc.JdbcEventDao;
import menuki.ticketing.data.jdbc.JdbcSeatDao;
import menuki.ticketing.model.Event;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * Service layer acting as the bridge between DAOs and the rest of the application
 * Handles business logic for Events
 */
public class JdbcEventService implements EventService {

    private final EventDao eventDao;
    private final SeatDao seatDao;


    //Constructors
    public JdbcEventService() {
        this(new JdbcEventDao(), new JdbcSeatDao());
    }

    public JdbcEventService(EventDao eventDao, SeatDao seatDao) {
        this.eventDao = eventDao;
        this.seatDao = seatDao;
    }

    /*
     * Business logic to get all events
     */
    @Override
    public List<Event> findAllEvents() {
        try {
            return eventDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load events", e);
        }
    }

    /*
     * Business logic to create an Event and add to the events table
     */
    public void createEvent(Event event) {
        try {
            eventDao.insert(event);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create event", e);
        }
    }



    /*
     * Generates unique IDs for events
     */
    @Override
    public String generateNextEventId() {
        try {
            return eventDao.getNextEventId();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate next event ID", e);
        }
    }

    @Override
    public void createEventWithSeating(Event event, List<String> vipRows, int vipCents, int stdCents) {
        //Create event using dao
        try {
            eventDao.insert(event);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert event", e);
        }

        SeatService seatSvc = new JdbcSeatService(seatDao);
        //Loading seat objects associated with a specific event using the service layer
        seatSvc.loadSeatsForEvent(event.getId());

        // Assign VIP/STANDARD seats
        seatSvc.markVipSeats(event.getId(), vipRows, vipCents);
        seatSvc.markStandardSeats(event.getId(), vipRows, stdCents);
    }


}
