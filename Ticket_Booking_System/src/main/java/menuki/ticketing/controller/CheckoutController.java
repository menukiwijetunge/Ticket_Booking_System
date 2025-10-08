
package menuki.ticketing.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import menuki.ticketing.model.Event;
import menuki.ticketing.model.Seat;
import menuki.ticketing.data.jdbc.JdbcOrderDao;
import menuki.ticketing.model.Session;
import menuki.ticketing.service.JdbcOrderService;
import menuki.ticketing.service.OrderService;

/**
 * Class that is the controller for checkouts and is directly linked to the checkout view
 */
public class CheckoutController {

    // Event labels
    @FXML private Label eventNameLabel;
    @FXML private Label eventIdLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventVenueLabel;
    // Seats table
    @FXML private TableView<SeatRow> seatsTable;
    @FXML private TableColumn<SeatRow, String> colSeatId;
    @FXML private TableColumn<SeatRow, String> colSeatType;
    @FXML private TableColumn<SeatRow, String> colPrice;
    // Total
    @FXML private Label totalLabel;

    private Event event;
    private List<Seat> seats = new ArrayList<>();
    private SeatMapController seatMapController;

    private final OrderService orderService = new JdbcOrderService(new JdbcOrderDao());


    /*
     * Sets the seat map controller
     */
    public void setSeatMapController(SeatMapController c) {
        this.seatMapController = c;
    }


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {
        colSeatId.setCellValueFactory(new PropertyValueFactory<>("seatId"));
        colSeatType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("priceAud"));
    }

    /*
     * This must be called before showing the stage and is used to populate the checkout screen
     */
    public void setData(Event event, List<Seat> seats) {
        this.event = event;
        this.seats = (seats != null) ? seats : new ArrayList<>();

        // Event fields
        eventNameLabel.setText(event.getName() != null ? event.getName() : "");
        eventIdLabel.setText(event.getId() != null ? event.getId() : "");
        if (event.getDate() != null) {
            eventDateLabel.setText(event.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            eventDateLabel.setText("-");
        }
        eventVenueLabel.setText(event.getVenue() != null ? event.getVenue() : "Main hall");

        // Seats table rows
        List<SeatRow> rows = new ArrayList<>();
        int totalCents = 0;
        for (Seat s : this.seats) {
            rows.add(new SeatRow(
                    s.getDisplayId(),
                    s.getType().name(),
                    centsToAud(s.getPriceCents())
            ));
            totalCents += s.getPriceCents();
        }
        seatsTable.getItems().setAll(rows);
        totalLabel.setText(centsToAud(totalCents));
    }


    /*
     * This is a helper that is used to convert cents to AUD
     */
    private String centsToAud(int cents) {
        return String.format("$%.2f AUD", cents / 100.0);
    }


    /*
     * Defines functionality that will take place when the Back button is clicked
     */
    @FXML
    private void onBack() {
        // simply close this window; caller (seat map) remains in background scene stack
        //Close the window while the seat map view remains
        Stage stage = (Stage) seatsTable.getScene().getWindow();
        stage.close();
    }


    /*
     * Defines functionality that will take place when the Confirm button is clicked
     */
    @FXML
    private void onConfirm() {
        try {
            //If no seats have been booked, alert
            if (event == null || seats == null || seats.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Nothing to confirm.").showAndWait();
                return;
            }

            // For safety ensure user is logged in before continuing
            String userId = Session.getCurrentUsername();
            if (userId == null || userId.isBlank()) {
                new Alert(Alert.AlertType.ERROR,
                        "No logged-in user found. Please login before booking.").showAndWait();
                return;
            }

            //making order and saving in DB via business logic in service layer

            int orderId = orderService.createOrder(userId, event.getId(), seats);

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Booking confirmed! Order #" + orderId, ButtonType.OK);
            ok.setHeaderText(null);
            ok.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to create order: " + ex.getMessage()).showAndWait();
            return;
        }

        Stage me = (Stage) seatsTable.getScene().getWindow();
        me.close();

        if (seatMapController != null) {
            seatMapController.finishAfterConfirmed();
        }
    }


    /*
     * This class is meant to support UI operations.
     * It uses abstraction to only get the data needed for the display
     * This is kept in the same file for simplicity and as it is not used anywhere else
     */
    public static class SeatRow {
        private final String seatId;
        private final String type;
        private final String priceAud;

        //Constructor
        public SeatRow(String seatId, String type, String priceAud) {
            this.seatId = seatId;
            this.type = type;
            this.priceAud = priceAud;
        }
        //Getters
        public String getSeatId() { return seatId; }
        public String getType()   { return type; }
        public String getPriceAud(){ return priceAud; }
    }
}
