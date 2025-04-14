package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavBarController {
    @FXML
    private quizcontroller parent;

    public void setParent(quizcontroller parent) {
        this.parent = parent;
    }

    public void handleHomeButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Controllers/ajouterquiz.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
