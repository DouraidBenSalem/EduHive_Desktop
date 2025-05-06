package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private String userRole;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println(userRole);
        if (userRole.equals("ROLE_ADMIN")) {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/Controllers/quizpage.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/style_css/style.css").toExternalForm());
            stage.setTitle("Eduhive");
            stage.setScene(scene);
            stage.show();
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/Controllers/take_quiz.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/style_css/style.css").toExternalForm());
            stage.setTitle("Eduhive");
            stage.setScene(scene);
            stage.show();
        }
        
    }

    public static void main(String[] args) {
        launch();
    }

    public void setUserRole(String role) {
        this.userRole = role;
    }
}
