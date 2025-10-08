package menuki.ticketing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.Node;
import javafx.stage.Stage;
import menuki.ticketing.model.Session;
import menuki.ticketing.service.LoginService;
import menuki.ticketing.data.jdbc.JdbcUserDao;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * This is the Base Abstract class that the admin and user dashboard controllers extend from. It has
 * functionality common to both of them.
 */
public abstract class BaseController {

    /*
     * Responsible for setting the welcome message label
     */
    protected void setUserLabel(Label label, String modeIfLoggedOut) {
        if (label == null) return;
        //Check the session for the current logged-in user
        String who = Session.isLoggedIn() ? Session.getCurrentUsername() : modeIfLoggedOut;
        //Admin vs regular user
        String prefix = "Admin".equalsIgnoreCase(modeIfLoggedOut) ? "Admin: " : "Welcome ";
        label.setText(prefix + who);
    }


    /*
     * Adds button per row in table
     */
    protected <T> void addButtonColumn(TableColumn<T, Void> column, String label, Consumer<T> handler) {
        column.setCellFactory(col -> new TableCell<T, Void>() {
            private final Button btn = new Button(label);
            {
                btn.setOnAction(e -> handler.accept(getTableView().getItems().get(getIndex())));
                btn.setMaxWidth(Double.MAX_VALUE);
            }
            //Display button as long as that row has data
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    /*
     * Defines logout functionality
     */
    @FXML
    protected void handleLogout(ActionEvent evt) {
        try {
            //Clear Session
            Session.clearSession();

            //Go back to 'login' screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof LoginController) {
                ((LoginController) ctrl).setLoginService(new LoginService(new JdbcUserDao()));
            }

            Stage stage = (Stage) ((Node) evt.getSource()).getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(root, 980, 640));
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load login screen", e);
        }
    }
}
