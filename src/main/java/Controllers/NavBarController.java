package Controllers;

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
