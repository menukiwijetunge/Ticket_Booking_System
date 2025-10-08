
package menuki.ticketing.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import menuki.ticketing.model.Event;
import java.time.LocalDate;
import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.data.jdbc.JdbcEventDao;
import menuki.ticketing.data.jdbc.JdbcOrderDao;
import menuki.ticketing.data.dao.OrderDao;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Map;
import java.util.List;

/**
 * This class is responsible for handling the admin dashboard and its functionalities.
 * This class extends the BaseController class that includes common operations
 * for both admin and user dashboard controllers.
 */

public class AdminDashboardController extends BaseController {
    //Label to show the current logged-in user
    @FXML private Label currentUserLabel;

    //Admin Dashboard Table Columns
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String>     colEventId;
    @FXML private TableColumn<Event, String>     colEventName;
    @FXML private TableColumn<Event, LocalDate>  colEventDate;
    @FXML private TableColumn<Event, Void>       colDelete;
    @FXML private TableColumn<Event, LocalTime> colStartTime;
    @FXML private TableColumn<Event, LocalTime> colEndTime;
    @FXML private TableColumn<Event, Number> colSeatsAvailable;


    private final EventDao eventDao = new JdbcEventDao();
    private final OrderDao orderDao = new JdbcOrderDao();


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {
        setUserLabel(currentUserLabel, "Admin");

        // Wiring table columns to properties of Event model
        colEventId.setCellValueFactory(new PropertyValueFactory<Event, String>("id"));
        colEventName.setCellValueFactory(new PropertyValueFactory<Event, String>("name"));
        colEventDate.setCellValueFactory(new PropertyValueFactory<Event, LocalDate>("date"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        addButtonColumn(colDelete, "Delete", this::onDeleteEvent);

        //Use to show up-to-date data in the table
        refreshEvents();
    }

    /*
     * Defines functionality that will take place when the Add Event button is clicked
     */
    @FXML
    private void onAddEvent() {
        try {
            //Loads FXML add event page/view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/new_event.fxml"));
            Parent root = loader.load();

            //Gets the respective controller of the view
            Object controller = loader.getController();
            if (controller instanceof NewEventController) {
                ((NewEventController) controller).setOnSaved(this::refreshEvents);
            }

            Stage stage = new Stage();
            stage.setTitle("New Event");
            stage.setScene(new Scene(root, 560, 420));
            stage.initOwner(eventsTable.getScene().getWindow());
            stage.setOnHidden(e -> refreshEvents());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to open New Event form: " + e.getMessage()).showAndWait();
        }
    }

    /*
     * Used to get the most up-to-date table data
     */
    private void refreshEvents() {
        try {
            //Get all events
            List<Event> list = eventDao.findAll();

            //Extract the number of available seats per event
            Map<String, Integer> availableByEvent = eventDao.countAvailableByEvent();

            //Update seat availability in table
            colSeatsAvailable.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            availableByEvent.getOrDefault(c.getValue().getId(), 0)
                    )
            );
            eventsTable.setItems(FXCollections.observableArrayList(list));

        } catch (SQLException ex) {
            ex.printStackTrace();
            eventsTable.setItems(FXCollections.observableArrayList());
            new Alert(Alert.AlertType.ERROR, "Failed to load events: " + ex.getMessage()).showAndWait();
        }
    }

    /*
     * Defines functionality that will take place when the Delete button is clicked
     */
    private void onDeleteEvent(Event ev) {
        String eventId = ev.getId();
        try {
            // Count existing orders for the event that is going to be deleted
            int booked = orderDao.countItemsForEvent(eventId);

            //Alert the admin that people have already booked this event
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    (booked > 0
                            ? "This event has " + booked + " booked seat(s). "
                            + "Deleting this event will delete those seat bookings. "
                            + "Would you still like to proceed?"
                            : "Delete this event?")
            );
            confirm.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            //First delete the order items
            orderDao.deleteOrderItemsByEvent(eventId);

            //Now delete the event
            eventDao.deleteById(eventId);
            //Update table info
            refreshEvents();

        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete: " + ex.getMessage()).showAndWait();
        }
    }
}
