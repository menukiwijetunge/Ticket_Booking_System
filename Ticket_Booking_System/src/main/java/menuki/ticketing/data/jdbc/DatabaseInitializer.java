package menuki.ticketing.data.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.model.Event;
import menuki.ticketing.model.User;
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


            SeatDao seatDao = new JdbcSeatDao();
            seatDao.createTableIfNotExists();

            JdbcOrderDao orderDao = new JdbcOrderDao();
            orderDao.createTableIfNotExists();

            JdbcOrderItemDao orderItemDao = new JdbcOrderItemDao();
            orderItemDao.createTableIfNotExists();

            if (!userDao.existsByUsername("admin")) {
                userDao.insert(new User("admin", "admin123", "ADMIN"));
            }
            if (!userDao.existsByUsername("user1")) {
                userDao.insert(new User("user1", "password", "USER"));
            }
            if (!userDao.existsByUsername("user2")) {
                userDao.insert(new User("user2", "password", "USER"));
            }

            final String DEFAULT_EVENT_ID = "E-2001";
            final String DEFAULT_EVENT_NAME = "Demo Event";
            final LocalDate DEFAULT_EVENT_DATE = LocalDate.parse("2025-10-08");
            final String DEFAULT_EVENT_VENUE = "Main Hall";
            final LocalTime DEFAULT_EVENT_START_TIME = LocalTime.parse("02:00");
            final LocalTime DEFAULT_EVENT_END_TIME = LocalTime.parse("03:00");

            //Ensures there is always at least one event in the database to make it easier for the demo
            if (!eventDao.existsById(DEFAULT_EVENT_ID)) {
                eventDao.insert(new Event(
                        DEFAULT_EVENT_ID,
                        DEFAULT_EVENT_NAME,
                        DEFAULT_EVENT_DATE,
                        DEFAULT_EVENT_VENUE,
                        DEFAULT_EVENT_START_TIME,
                        DEFAULT_EVENT_END_TIME,
                        236
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

