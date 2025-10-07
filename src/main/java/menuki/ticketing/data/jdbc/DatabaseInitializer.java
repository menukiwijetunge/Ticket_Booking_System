package menuki.ticketing.data.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.model.Event;
import menuki.ticketing.service.JdbcSeatService;

/**
 * Class responsible for setting up the database and preparing it for the program to use
 */
public final class DatabaseInitializer {

    private static final String DB_NAME = "ticketingSystem";

    /*
     * Used to prevent class from being instantiated
     */
    private DatabaseInitializer() {
    }

    /*
     * Creates the database and the tables if they are not created
     */
    public static void initialize() {
        try {
           //Ensure if the DB exists
            try (Connection root = DatabaseConnection.getRootConnection();
                 Statement st = root.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            //Create tables if they do not exist and populate with dummy data where needed
            JdbcEventDao eventDao = new JdbcEventDao();
            eventDao.createTableIfNotExists();

            JdbcUserDao userDao = new JdbcUserDao();
            userDao.createTableIfNotExists();
            userDao.insertDefaultUsers();

            SeatDao seatDao = new JdbcSeatDao();
            seatDao.createTableIfNotExists();

            JdbcOrderDao orderDao = new JdbcOrderDao();
            orderDao.createTableIfNotExists();

            JdbcOrderItemDao orderItemDao = new JdbcOrderItemDao();
            orderItemDao.createTableIfNotExists();



            final String DEFAULT_EVENT_ID = "E-2001";
            final String DEFAULT_EVENT_NAME = "Demo Event";
            final String DEFAULT_EVENT_VENUE = "Main Hall";

            //Ensures there is always at least one event in the database to make it easier for the demo
            if (!eventDao.existsById(DEFAULT_EVENT_ID)) {
                eventDao.insert(new Event(
                        DEFAULT_EVENT_ID,
                        DEFAULT_EVENT_NAME,
                        null,
                        DEFAULT_EVENT_VENUE,
                        null,
                        null,
                        0
                ));
                System.out.println("Seeded default event: " + DEFAULT_EVENT_ID);
            }


            // Generate seats for that event if none exist yet
            JdbcSeatService seatService = new JdbcSeatService(seatDao);
            seatService.loadSeatsForEvent(DEFAULT_EVENT_ID);
            System.out.println("Seeded seats for event: " + DEFAULT_EVENT_ID);
            System.out.println("Database initialized.");
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}

