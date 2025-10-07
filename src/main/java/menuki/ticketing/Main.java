
package menuki.ticketing;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import menuki.ticketing.controller.LoginController;
import menuki.ticketing.data.jdbc.DatabaseInitializer;
import menuki.ticketing.data.jdbc.JdbcUserDao;   // needed
import menuki.ticketing.service.LoginService;

public class Main extends Application {

    private static final LoginService LOGIN_SERVICE = new LoginService(new JdbcUserDao());
    public static LoginService loginService() { return LOGIN_SERVICE; }
    @Override
    public void start(Stage stage) {
        try {
            // Consider moving heavy DB work off the FX thread in a Task if it grows
            DatabaseInitializer.initialize();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load());

            LoginController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("LoginController not initialized (check fx:controller in FXML).");
            }

//            // DI wiring: swap implementations here to demonstrate polymorphism
//            controller.setLoginService(new LoginService(new JdbcUserDao()));

            controller.setLoginService(loginService());
            // For tests/offline:
             // controller.setLoginService(new LoginService(new InMemoryUserDao()));

            stage.setTitle("Ticketing System - Login");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            // Don’t swallow errors—fail fast with useful info
            e.printStackTrace();
            throw new RuntimeException("App startup failed", e);
        }
    }
}