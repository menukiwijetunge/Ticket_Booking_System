package menuki.ticketing.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import menuki.ticketing.model.Order;
import menuki.ticketing.model.User;
import menuki.ticketing.service.OrderService;

/**
 * This class is the controller for the order view, which shows all the orders of the current user.
 */
public class OrdersController {
    // Table columns
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String>  colCreated;
    @FXML private TableColumn<Order, Void>    colAction;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private User currentUser;
    private OrderService orderService;

    /*
     * This is called by the dashboard when this view is opened
     */
    public void setContext(User user, OrderService orderService) {
        this.currentUser = user;
        this.orderService = orderService;
        loadData();
    }


    /*
     * This is automatically called on the controller after loading the FXML and injects the @FXML fields
     */
    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // CreatedAt (formatted)
        colCreated.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getCreatedAt() == null
                                ? ""
                                : dtf.format(cell.getValue().getCreatedAt())
                )
        );


        // "View" for each order
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("View");
            {
                btn.getStyleClass().add("button");
                btn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    onViewClicked(order);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    /*
     * Used to populate the orders onto the screen
     */
    private void loadData() {
        if (currentUser == null || orderService == null) return;
        List<Order> rows = orderService.findByUser(currentUser.getUsername());
        ordersTable.setItems(FXCollections.observableArrayList(rows));
    }


    /*
     * Defines functionality that will take place when the View button is clicked
     */
    private void onViewClicked(Order order) {
        try {
            //Loads the order_details view to view order specific details
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/order_details.fxml"));
            Parent root = loader.load();

            // Get the controller
            OrderDetailsController controller = loader.getController();

            // Initialize with order and DAOs
            controller.initData(
                    order,
                    new menuki.ticketing.data.jdbc.JdbcOrderItemDao(),
                    new menuki.ticketing.data.jdbc.JdbcEventDao()
            );

            Stage stage = new Stage();
            stage.setTitle("Order #" + order.getId());
            stage.setScene(new Scene(root, 700, 520));
            stage.initOwner(ordersTable.getScene().getWindow());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open order details: " + e.getMessage()).showAndWait();
        }
    }


    /*
     * Defines functionality that will take place when the Close button is clicked
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        stage.close();
    }
}
