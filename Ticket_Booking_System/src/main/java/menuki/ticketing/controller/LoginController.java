package menuki.ticketing.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Map;
import menuki.ticketing.model.User;
import menuki.ticketing.service.LoginService;
import menuki.ticketing.model.Session;

/**
 * Controller for login
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginService loginService;

    /*
     * Sets loginService which is injected from Main
     */
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

//----------------------------------------------------------------------------------------------------------------------
    /*
     * Dashboard navigator is an interface that helps utilize polymorphism to identify which dashboard controller to use.
     * Kept private and encapsulated as it is login-specific.
     */
    private interface DashboardNavigator {
        /*
         * This function is used to identify and load the appropriate dashboard
         */
        void go(Stage stage) throws IOException;
    }
//----------------------------------------------------------------------------------------------------------------------


    /* Building a lookup table
     * If Admin -> admin dashboard
     * If user-> user dashboard
     * After logging in use this map to look up the person;s role and do what is required.
     */
    private final Map<String, DashboardNavigator> navigators = Map.of(
            "ADMIN", stage -> loadScene(stage, "/views/admin_dashboard.fxml", "Admin"),
            "USER",  stage -> loadScene(stage, "/views/user_dashboard.fxml", "Home")
    );

    /*
     * Helper that loads a scene using the FXML path
     */
     private void loadScene(Stage stage, String fxmlPath, String title) throws IOException {
        if (getClass().getResource(fxmlPath) == null) {
            throw new IllegalStateException("FXML not found on classpath: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setMaximized(true);

    }


    /*
     * Function that handles login
     */
    @FXML
    private void handleLogin() {
        if (loginService == null) {
            alert("Internal error: service not initialized.");
            return;
        }
        // Get user entry for credentials
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Ensure both username and password is entered
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            alert("Please enter both username and password.");
            return;
        }

        // Authenticate user
        User authenticatedUser = loginService.authenticate(username, password);
        if (authenticatedUser == null) {
            new Alert(Alert.AlertType.ERROR, "Invalid credentials.").showAndWait();
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        // If user successfully authenticated create session
        Session.setCurrentUsername(authenticatedUser.getUsername());

        // Use polymorphism to load the appropriate dashboard
        String role = authenticatedUser.getRole() == null ? "" : authenticatedUser.getRole().toUpperCase();
        DashboardNavigator nav = navigators.get(role);
        if (nav == null) nav = navigators.get("USER"); //Default dashboard wil be the (regular) user dashboard
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            nav.go(stage);
        } catch (IOException e) {
            e.printStackTrace();
            alert("Failed to load dashboard: " + e.getMessage());
        }
    }
    /*
     * Used to display any alerts
     */
    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }


}

