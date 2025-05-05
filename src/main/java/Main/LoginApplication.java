package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/Controllers/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Eduhive");
        stage.setScene(scene);
        stage.show();
    }
}
