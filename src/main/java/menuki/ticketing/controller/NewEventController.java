package menuki.ticketing.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.data.dao.SeatDao;
import menuki.ticketing.data.jdbc.JdbcEventDao;
import menuki.ticketing.data.jdbc.JdbcSeatDao;
import menuki.ticketing.model.Event;
import menuki.ticketing.service.EventService;
import menuki.ticketing.service.JdbcEventService;
import menuki.ticketing.service.JdbcSeatService;
import menuki.ticketing.service.SeatService;

/**
 * This class is the controller for the functionality of adding a new event
 * which is done by the admin and directly related to the new_event view
 */
public class NewEventController {
    //Fields
    @FXML private TextField      txtName;
    @FXML private DatePicker     dpDate;
    @FXML private ComboBox<String> cbStartTime;
    @FXML private ComboBox<String> cbEndTime;
    @FXML private TextField      txtVipPrice;
    @FXML private TextField      txtStdPrice;


    private final EventDao eventDao = new JdbcEventDao();
    private final SeatDao seatDao = new JdbcSeatDao();

    private final EventService eventService = new JdbcEventService(eventDao, seatDao);
    private final SeatService seatService = new JdbcSeatService(seatDao);



    //Used when updating the events table after saving the new event
    private Runnable onSaved;

    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {
        //Time slots are in 30 minute blocks
        List<String> times = new ArrayList<>();
        for (int m = 0; m < 24 * 60; m += 30) {
            times.add(LocalTime.MIDNIGHT.plusMinutes(m).toString()); // "HH:mm"
        }
        //Options for start and end times
        cbStartTime.setItems(FXCollections.observableArrayList(times));
        cbEndTime.setItems(FXCollections.observableArrayList(times));

        //Default start and end time selections
        cbStartTime.getSelectionModel().select("18:00");
        cbEndTime.getSelectionModel().select("21:00");

        // Money input formatting
        setupMoneyFormatter(txtVipPrice);
        setupMoneyFormatter(txtStdPrice);

        //Default date is the current (today's) date
        dpDate.setValue(LocalDate.now());
    }


    /*
     * This is the setter for the callback. The code passed should run after the new event is saved
     */
    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

   /*
    * Used for money formatting. Validates the input to ensure it can be accepted as money format
    */
    private void setupMoneyFormatter(TextField tf) {
        TextFormatter<String> fmt = new TextFormatter<>(change -> {
            String s = change.getControlNewText();
            if (s.isEmpty()) return change;
            if (!s.matches("\\d{0,6}(?:\\.\\d{0,2})?")) return null;
            return change;
        });
        tf.setTextFormatter(fmt);
    }


    /*
     * Defines functionality that will take place when the Save button is clicked
     */
    @FXML
    private void handleSave() {
        //Getting values of the input fields (trim when needed)
        String name   = txtName.getText() == null ? "" : txtName.getText().trim();
        LocalDate date = dpDate.getValue();
        String startStr = cbStartTime.getValue();
        String endStr   = cbEndTime.getValue();

        //Ensure no fields are empty
        if (name.isEmpty() || date == null || startStr == null || endStr == null ||
                startStr.isEmpty() || endStr.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill Event Name, Date, Start Time, and End Time.").showAndWait();
            return;
        }

        LocalTime startTime = LocalTime.parse(startStr);
        LocalTime endTime   = LocalTime.parse(endStr);

        //Ensures End time comes after Start time
        if (!endTime.isAfter(startTime)) {
            new Alert(Alert.AlertType.WARNING, "End time must be after start time.").showAndWait();
            return;
        }


        int vipCents = parseDollarsToCents(txtVipPrice.getText());
        int stdCents = parseDollarsToCents(txtStdPrice.getText());

        //Ensures prices are positive
        if (vipCents <= 0 || stdCents <= 0) {
            new Alert(Alert.AlertType.WARNING, "Please enter positive VIP and Standard prices.").showAndWait();
            return;
        }

        // Which rows will be VIP rows
        final List<String> vipRows = List.of("G", "H");

        //Temporarily disable fields while saving
        txtName.setDisable(true);
        dpDate.setDisable(true);
        cbStartTime.setDisable(true);
        cbEndTime.setDisable(true);
        txtVipPrice.setDisable(true);
        txtStdPrice.setDisable(true);

        // Background job (runs off the UI thread so the app doesn't freeze while saving)
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {


                String eventId = eventService.generateNextEventId();
                Event event = new Event(eventId, name, date, null, startTime, endTime, 0);

                // Creating event and seating setup is covered by the service layer
                eventService.createEventWithSeating(event, vipRows, vipCents, stdCents);
                return null;


            }

        };
        // Attaching UI thread handlers for the background Task
        task.setOnSucceeded(e -> {
            if (onSaved != null) onSaved.run();
            Stage s = (Stage) txtName.getScene().getWindow();
            s.close();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            new Alert(Alert.AlertType.ERROR, "Failed to save event: " + (ex != null ? ex.getMessage() : "")).showAndWait();
            // Re-enable form so user can retry
            txtName.setDisable(false);
            dpDate.setDisable(false);
            cbStartTime.setDisable(false);
            cbEndTime.setDisable(false);
            txtVipPrice.setDisable(false);
            txtStdPrice.setDisable(false);
        });
        //Ensures we launch background task without freezing the UI
        new Thread(task, "save-event-task").start();
    }

    /*
     * This ensures that the money value is valid so it takes a string and converts into a safe format
     */
    private int parseDollarsToCents(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            double d = Double.parseDouble(s);
            long cents = Math.round(d * 100.0);
            if (cents < 0 || cents > Integer.MAX_VALUE) return 0;
            return (int) cents;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /*
     * Defines functionality that will take place when the Cancel button is clicked
     */
    @FXML
    private void handleCancel() {
        Stage s = (Stage) txtName.getScene().getWindow();
        s.close();
    }
}
