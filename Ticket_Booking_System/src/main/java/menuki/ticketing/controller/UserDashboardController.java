package menuki.ticketing.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.data.jdbc.JdbcOrderDao;
import menuki.ticketing.data.jdbc.JdbcSeatDao;
import menuki.ticketing.model.*;
import menuki.ticketing.service.JdbcOrderService;
import menuki.ticketing.service.JdbcSeatService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.data.jdbc.JdbcEventDao;
import menuki.ticketing.service.OrderService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * This class is responsible for handling the regular user dashboard and its functionalities.
 * This class extends the BaseController class that includes common operations
 * for both admin and user dashboard controllers.
 */
public class UserDashboardController extends BaseController {
    //Label to show the current logged-in user
    @FXML private Label currentUserLabel;
    @FXML private Label statusLabel;

    //User Dashboard Table Columns
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String>     colEventId;
    @FXML private TableColumn<Event, String>     colEventName;
    @FXML private TableColumn<Event, LocalDate>  colEventDate;
    @FXML private TableColumn<Event, Number>     colSeats;
    @FXML private TableColumn<Event, Void>       colView;

    private final EventDao eventDao = new JdbcEventDao();
    private final SeatDao seatDao = new JdbcSeatDao();
    //Used for identifying available seats
    private final Map<String, Integer> availableByEvent = new HashMap<String, Integer>();


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {

        setUserLabel(currentUserLabel, "Guest");

        // Wiring table columns to properties of Event model
        colEventId.setCellValueFactory(new PropertyValueFactory<Event, String>("id"));
        colEventName.setCellValueFactory(new PropertyValueFactory<Event, String>("name"));
        colEventDate.setCellValueFactory(new PropertyValueFactory<Event, LocalDate>("date"));
        colSeats.setText("Available Seats");
        colSeats.setCellValueFactory(c ->
                new SimpleObjectProperty<Number>(availableByEvent.getOrDefault(c.getValue().getId(), 0)));
        addButtonColumn(colView, "View", this::onViewEvent);

        //Use to show up-to-date data in the table
        refreshEvents();
    }


    /*
     * Used to get the most up-to-date table data
     */
    private void refreshEvents() {
        try {
            //Get all events
            List<Event> list = eventDao.findAll();

            availableByEvent.clear();
            try {

                availableByEvent.putAll(seatDao.countAvailableByEvent());
            } catch (SQLException ignored) { }
            //Populating the events table again
            eventsTable.setItems(FXCollections.<Event>observableArrayList(list));
            if (statusLabel != null) {
                statusLabel.setText("Loaded events: " + list.size());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            eventsTable.setItems(FXCollections.<Event>observableArrayList());
            if (statusLabel != null) {
                statusLabel.setText("Failed to load events: " + ex.getMessage());
            }
            new Alert(Alert.AlertType.ERROR, "Failed to load events: " + ex.getMessage()).showAndWait();
        }
    }

    /*
     * Defines functionality that will take place when the View button is clicked
     */
    private void onViewEvent(Event ev) {
        try {
            // Opens the seat_map view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/seat_map.fxml"));
            Parent root = loader.load();

            //Controller management
            SeatMapController ctrl = loader.getController();
            ctrl.setSeatService(new JdbcSeatService());
            ctrl.initForEvent(ev.getId(), ev.getName(), ev.getDate(), ev.getVenue(), ev.getStartTime(), ev.getEndTime());

            Stage stage = (Stage) eventsTable.getScene().getWindow();
            stage.setTitle("Seating – " + ev.getName());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Failed to open seat map: " + e.getMessage());
            }
        }
    }


    /*
     * Defines functionality that will take place when the Orders button is clicked
     */
    @FXML
    private void onOrders() {
        try {
            // Load the orders view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/orders.fxml"));
            Parent root = loader.load();

            OrderService orderService = new JdbcOrderService(new JdbcOrderDao());
            // For safety ensuring that there is a proper logged-in user before displaying orders
            String uname = Session.getCurrentUsername();
            if (uname == null || uname.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "You must be logged in to view orders.").showAndWait();
                return;
            }
            User u = new User(uname, "", "USER");


            //Controller management
            OrdersController ctrl = loader.getController();
            ctrl.setContext(u, orderService);

            Stage stage = new Stage();
            stage.setTitle("My Orders");
//            stage.setScene(new Scene(root, 760, 520));
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.initOwner(eventsTable.getScene().getWindow());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Failed to open Orders: " + e.getMessage());
            }
            new Alert(Alert.AlertType.ERROR, "Failed to open Orders: " + e.getMessage()).showAndWait();
        }
    }
}
