package Controllers;

import Main.Main;
import Main.LoginApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.MenuItem;

import java.io.IOException;

public class NavBarController {
    @FXML
    private quizcontroller parent;

    public void setParent(quizcontroller parent) {
        this.parent = parent;
    }

    private void navigateToPage(String fxmlPath, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Controllers/" + fxmlPath));
        Stage stage;
        if (event.getSource() instanceof MenuItem) {
            stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        } else {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        }
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void handleLogoutButton(ActionEvent event) throws IOException {
        // Close the current window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Close all other open windows from the application
        for (Stage openStage : javafx.stage.Window.getWindows().filtered(window -> window instanceof Stage).stream()
                .map(window -> (Stage) window)
                .toList()) {
            openStage.close();
        }
        
        // Start the login application in a new stage
        LoginApplication mainApp = new LoginApplication();
        mainApp.start(new Stage());
    }

    @FXML
    public void handleHomeButton(ActionEvent event) throws IOException {
        navigateToPage("resultatpage.fxml", event);
    }
    
    @FXML
    public void navigateToQuiz(ActionEvent event) throws IOException {
        navigateToPage("quizpage.fxml", event);
    }

    @FXML
    public void navigateToUser(ActionEvent event) throws IOException {
        navigateToPage("userpage.fxml", event);
    }

    @FXML
    public void navigateToAnnouncement(ActionEvent event) throws IOException {
        navigateToPage("announcementpage.fxml", event);
    }
}
