package menuki.ticketing.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import menuki.ticketing.data.dao.EventDao;
import menuki.ticketing.data.dao.OrderItemDao;
import menuki.ticketing.model.Event;
import menuki.ticketing.model.Order;
import menuki.ticketing.model.OrderItem;


/**
 * This class is the controller for the order_details view, which shows details of a specific user order.
 */
public class OrderDetailsController {

    // Title
    @FXML private Label eventTitle;

    // Summary labels
    @FXML private Label lblOrderId;
    @FXML private Label lblUser;
    @FXML private Label lblEvent;
    @FXML private Label lblDate;
    @FXML private Label lblNote;

    // Subtotal
    @FXML private Label lblSubtotal;

    // Table columns
    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> colSeat;
    @FXML private TableColumn<OrderItem, String> colPrice;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Order order;
    private OrderItemDao orderItemDao;
    private EventDao eventDao;

    /*
     *This is called from OrdersController after FXML load.
     */
    public void initData(Order order, OrderItemDao orderItemDao, EventDao eventDao) {
        this.order = order;
        this.orderItemDao = orderItemDao;
        this.eventDao = eventDao;
        loadData();
    }


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {
        // Seat formatted like A-02
        colSeat.setCellValueFactory(oi ->
                new SimpleStringProperty(formatSeat(oi.getValue().getRowLabel(), oi.getValue().getSeatNumber()))
        );
        // lblNote is used to display a message when that event is deleted
        if (lblNote != null) {
            lblNote.managedProperty().bind(lblNote.visibleProperty());
            lblNote.setVisible(false);
        }

        // Price column formatted
        colPrice.setCellValueFactory(oi ->
                new SimpleStringProperty(String.format("$%.2f", oi.getValue().getPriceCents() / 100.0))
        );
    }


   /*
    * Used to populate the order details data onto the screen
    */
    private void loadData() {
        if (order == null) return;
        //Getting all those order details
        lblOrderId.setText(String.valueOf(order.getId()));
        lblUser.setText(
                order.getUserId() != null && !order.getUserId().isBlank()
                        ? order.getUserId()
                        : (order.getUserId() == null ? "-" : order.getUserId())
        );

        try {
            // Load order items
            List<OrderItem> items = orderItemDao.findByOrderId(order.getId());
            itemsTable.setItems(FXCollections.observableArrayList(items));
            //Add that no items message if there are no items resulting from event cancellation
            lblNote.setVisible(items.isEmpty());

            // Subtotal
            int totalCents = items.stream().mapToInt(OrderItem::getPriceCents).sum();
            lblSubtotal.setText(String.format("$%.2f", totalCents / 100.0));

            // Event details
            String eventId = items.isEmpty() ? null : items.get(0).getEventId();
            if (eventId != null) {
                Event ev = eventDao.findById(eventId);
                if (ev != null) {
                    eventTitle.setText(ev.getName());
                    lblEvent.setText(ev.getName() + " (" + ev.getId() + ")");
                    lblDate.setText(ev.getDate() != null ? dateFmt.format(ev.getDate()) : "-");
                } else {
                    eventTitle.setText(eventId);
                    lblEvent.setText(eventId);
                    lblDate.setText("-");
                }
            } else {
                eventTitle.setText("Order Details");
                lblEvent.setText("-");
                lblDate.setText("-");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load order details: " + e.getMessage()).showAndWait();
        }
    }

    /*
     * Used to format the seat using rowLabel and seatNumber
     */
    private String formatSeat(String rowLabel, int seatNumber) {
        if (rowLabel == null) rowLabel = "-";
        return String.format("%s-%02d", rowLabel, seatNumber);
    }

    /*
     * Defines functionality that will take place when the Close button is clicked
     */
    @FXML
    private void handleClose() {
        Stage s = (Stage) itemsTable.getScene().getWindow();
        s.close();
    }
}
