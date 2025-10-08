package menuki.ticketing.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import menuki.ticketing.model.*;
import menuki.ticketing.service.SeatService;
import javafx.scene.control.OverrunStyle;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import menuki.ticketing.model.Event;
import menuki.ticketing.model.Seat;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is the controller is for the Seat_map view from which users can book seats.
 */
public class SeatMapController {

    @FXML private GridPane seatGrid;
    @FXML private Label eventNameLabel;
    @FXML private Label seatLabel;
    @FXML private Label typeLabel;
    @FXML private Label priceLabel;
    @FXML private Button addToCartBtn;
    @FXML private ListView<String> cartList;
    @FXML private Label statusLabel;

    private SeatService seatService;
    private String eventId;
    private String eventName;
    private String venue;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    // UI state
    private final Map<String, Button> seatButtons = new HashMap<>();
    private final Map<String, Seat> seatById = new HashMap<>();
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private final List<String> cart = new ArrayList<>();

    /*
     * Used for injecting the seatService (done from a different controller)
     */
    public void setSeatService(SeatService seatService) {
        this.seatService = seatService;
    }

    /*
     * Initializing the seat map screen for that event
     */
    public void initForEvent(String eventId, String eventName, LocalDate date, String venue, LocalTime startTime, LocalTime endTime ) {

        if (this.seatService == null) {
            throw new IllegalStateException("SeatService not set");
        }

        this.eventId = eventId;
        this.eventName = eventName;
        this.venue = venue;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        //Update UI header
        if (eventNameLabel != null) eventNameLabel.setText(eventName);
        //Build the seat map
        renderGrid();
    }


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {

        //Setting up the seatGrid
        if (seatGrid != null) {
            seatGrid.setAlignment(Pos.TOP_CENTER);
            seatGrid.setPadding(new Insets(10));
        }
        //Disable add to cart button as long as no items are added
        if (addToCartBtn != null) addToCartBtn.setDisable(true);

    }

    /*
     * Builds seat map for the selected event
     */
    private void renderGrid() {
        seatGrid.getChildren().clear();
        seatButtons.clear();
        seatById.clear();
        selectedSeats.clear();

        //Get the seats grouped by row & flatten to a single list for UI prep
        Map<String, List<Seat>> byRow = seatService.loadSeatsGroupedByRow(eventId);
        List<Seat> seats = byRow.values().stream().flatMap(List::stream).toList();


        //Store every seat in a map with the key being the display ID
        seats.forEach(s -> seatById.put(s.getDisplayId(), s));



        int maxWidth = byRow.values().stream().mapToInt(List::size).max().orElse(0);
        int rowIndex = 0;
        for (List<Seat> rowSeats : byRow.values()) {
            //Seats within a row should be ordered smallest to largest (left to right)
            rowSeats.sort(Comparator.comparingInt(Seat::getSeat_number));

            // Padding to center the row
            int pad = (maxWidth - rowSeats.size()) / 2;

            int colIndex = 0;
            //Adding left side blank to center shorter rows
            for (int i = 0; i < pad; i++) {
                Region spacer = new Region();
                spacer.setMinSize(10, 10);
                GridPane.setHalignment(spacer, HPos.CENTER);
                seatGrid.add(spacer, colIndex++, rowIndex);
            }
            // Display a button for each seat
            for (Seat s : rowSeats) {
                Button btn = makeSeatButton(s);
                seatButtons.put(s.getDisplayId(), btn);
                seatGrid.add(btn, colIndex++, rowIndex);
            }

            rowIndex++;
        }

        updateButtons();
        status("Loaded " + seats.size() + " seats.");
    }



    /*
     * Used to make a button for each seat
     */
    private Button makeSeatButton(Seat s) {

        Button btn = new Button(s.getDisplayId());
        // Widen so the number fits
        btn.setPrefSize(52, 38);
        btn.setMinWidth(52);
        btn.setMaxWidth(52);

        //If text to be displayed on seat is too long, just cut off extra characters
        btn.setTextOverrun(OverrunStyle.CLIP);
        btn.setStyle(styleFor(s, false) + " -fx-font-size: 11px;");

        //Disable already reserved seats
        btn.setDisable(s.getStatus() == SeatStatus.RESERVED);
        //If a seat is selected then call onSeatClicked
        btn.setOnAction(e -> onSeatClicked(s));
        return btn;
    }

    /*
     * Defines functionality that will take place when the seat is clicked
     */
    private void onSeatClicked(Seat seat) {
        //Cannot select reserved seats
        if (seat.getStatus() == SeatStatus.RESERVED) return; // disabled

        //Toggle (select /de-select seats)
        String did = seat.getDisplayId();
        if (selectedSeats.contains(did)) {
            selectedSeats.remove(did);
        } else {
            selectedSeats.add(did);
        }
        showSeatDetails(seat);
        updateButtons();
    }

    /*
     * Used to display details of a selected seat
     */
    private void showSeatDetails(Seat seat) {
        //Seat Info
        seatLabel.setText(seat.getDisplayId());
        typeLabel.setText(seat.getType().name());
        priceLabel.setText(NumberFormat.getCurrencyInstance().format(seat.getPriceCents() / 100.0));

        //Add to cart button should be disabled if no seats selected
        if (addToCartBtn != null) {
            addToCartBtn.setDisable(selectedSeats.isEmpty());
        }
    }

    /*
     *Adjust button styling and whether it is enabled/disabled based on seat status/type
     */
    private void updateButtons() {

        for (Seat s : seatById.values()) {
            Button b = seatButtons.get(s.getDisplayId());
            boolean isSelected = selectedSeats.contains(s.getDisplayId());
            b.setStyle(styleFor(s, isSelected));
            b.setDisable(s.getStatus() == SeatStatus.RESERVED);
        }
        //Add to cart button should be disabled if no seats selected
        if (addToCartBtn != null) addToCartBtn.setDisable(selectedSeats.isEmpty());
    }

    /*
     * Defines styling based on button type/status
     */
    private String styleFor(Seat s, boolean isSelected) {
        String base = "-fx-pref-width:38; -fx-pref-height:38; "
                + "-fx-background-radius:6; -fx-border-radius:6; -fx-border-color:#999;";
        String bg   = (s.getStatus() == SeatStatus.RESERVED)
                ? "-fx-background-color:#c7c7c7; -fx-opacity:0.85;"
                : isSelected ? "-fx-background-color:#a0c4ff;"
                : "-fx-background-color:#ffffff;";
        String vip  = (s.getType() == SeatType.VIP) ? " -fx-border-color:#f59e0b; -fx-border-width:2;" : "";
        return base + " " + bg + vip;
    }

    /*
     * Defines functionality that will take place when the Add To Cart button is clicked
     */
    @FXML
    private void onAddToCart() {
        //If no seats are selected this cannot be done
        if (selectedSeats.isEmpty()) return;
        List<String> ids = new ArrayList<>(selectedSeats);
        boolean ok = seatService.reserveSeats(eventId, ids);

        //If not able to reserve seats.
        if (!ok) {
            status("Some seats were already booked by someone else. Refreshing…");
            selectedSeats.clear();
            renderGrid();
            return;
        }
        //Utilizing defined business logic to mark seats as reserved
        seatService.markReserved(ids.stream().map(seatById::get).toList());

        //Add selected seats to cart
        cart.addAll(ids);
        cartList.getItems().setAll(cart);

        //Update seat from selected to reserved
        selectedSeats.clear();
        updateButtons();

        if (addToCartBtn != null) addToCartBtn.setDisable(true);

        status("Booked: " + String.join(", ", ids));
    }



    /*
     * Defines functionality that will take place when the Clear cart button is clicked
     */
    @FXML
    private void onClearCart() {
        if (cart.isEmpty()) {
            status("Cart cleared.");
            //Disable add to cart button
            if (addToCartBtn != null) addToCartBtn.setDisable(true);
            return;
        }

        // Release these seats in DB
        List<String> ids = new ArrayList<>(cart);
        //Utilizing business logic from service layer to release seats and mark as available
        seatService.releaseSeats(eventId, ids);
        seatService.markAvailable(ids.stream().map(seatById::get).toList());

        cart.clear();
        cartList.getItems().clear();
        selectedSeats.clear();
        updateButtons();
        //Disable add to cart button
        if (addToCartBtn != null) addToCartBtn.setDisable(true);
        status("Cart cleared; seats released.");
    }


    /*
     * Used to update status label with the message passed (UI purposes)
     */
    private void status(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
        System.out.println(msg);
    }

    /*
     * Defines functionality that will take place when the Checkout button is clicked
     */
    @FXML
    private void onCheckout() {
        try {
            //If trying to check out with an empty cart
            if (cart == null || cart.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Your cart is empty. Please add seats first.").showAndWait();
                return;
            }

            // Load checkout view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/checkout.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("FXML not found: /views/checkout.fxml");
            }
            Parent root = loader.load();

            CheckoutController ctrl = loader.getController();

            // Create event object for checkout screen (to be supplied to CheckoutController)
            // Num seats not relevant here
            Event ev = new Event(this.eventId, this.eventName, this.date, this.venue, this.startTime, this.endTime, 0);

            //Turning cart seat IDs to seat objects
            List<Seat> cartSeats = new ArrayList<>();
            for (String did : cart) {
                Seat s = seatById.get(did);
                if (s != null) cartSeats.add(s);
            }


            ctrl.setData(ev, cartSeats);
            ctrl.setSeatMapController(this);

            // Show checkout window
            Stage dialog = new Stage();
            dialog.setTitle("Checkout");
            dialog.setScene(new Scene(root, 900, 600));
            dialog.initOwner(seatGrid.getScene().getWindow());
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open checkout: " + e.getMessage()).showAndWait();
        }
    }


    /*
     * Defines functionality that will take place when the Back button is clicked
     */
    @FXML
    private void onBack() {
        try {
            // If cart has items, inform user and get a confirmation
            if (!cart.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Leave seat selection?");
                alert.setHeaderText(null);
                alert.setContentText("Your cart has " + cart.size() + " seat(s). Going back now will discard your seats.");
                ButtonType discard = new ButtonType("Discard & Go Back", ButtonBar.ButtonData.OK_DONE);
                ButtonType stay     = new ButtonType("Stay", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(discard, stay);

                Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() == stay) {
                    //If user said to click cancel remain
                    return;
                }

                // User confirmed, release seats in DB and also update the local state accordingly
                List<String> ids = new ArrayList<>(cart);
                try {
                    seatService.releaseSeats(eventId, ids);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                //Utilizing business logic from service layer (to mark seats as available)
                seatService.markAvailable(ids.stream().map(seatById::get).toList());

                cart.clear();
                cartList.getItems().clear();
                selectedSeats.clear();
                updateButtons();
                status("Cart discarded; seats released.");
            }

            // Return back to the user dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("FXML not found: /views/user_dashboard.fxml");
            }

            Parent root = loader.load();

            Stage stage = (Stage) seatGrid.getScene().getWindow();
            stage.setTitle("User Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to go back: " + e.getMessage()).showAndWait();
        }
    }


    /*
     * Cleaning and Clearing UI State after successfully Checking out
     */
    public void finishAfterConfirmed() {
        try {
            //Clear Cart
            cart.clear();
            if (cartList != null) cartList.getItems().clear();
            selectedSeats.clear();

            //Load user Dashboard

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("FXML not found: /views/user_dashboard.fxml");
            }
            Parent root = loader.load();

            Stage stage = (Stage) seatGrid.getScene().getWindow();
            stage.setTitle("User Dashboard");
            stage.setScene(new Scene(root, 980, 640));
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to return to dashboard: " + e.getMessage()).showAndWait();
        }
    }



}
