package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class frontnavcontrollers {
    
    @FXML
    private Button homeButton;
    
    @FXML
    private Button quizButton;
    
    @FXML
    private Button aiQuizButton;
    
    @FXML
    private Button matiereButton;
    
    @FXML
    private Button coursButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    void handleHomeButton(ActionEvent event) {
        navigateTo("/Controllers/home.fxml");
    }
    
    @FXML
    void handleQuizButton(ActionEvent event) {
        navigateTo("/Controllers/quizpage.fxml");
    }
    
    @FXML
    void handleAIQuizButton(ActionEvent event) {
        navigateTo("/Controllers/ai_quiz_generator.fxml");
    }
    
    @FXML
    void handleMatiereButton(ActionEvent event) {
        // Navigate to matiere page when implemented
        // navigateTo("/Controllers/matiere.fxml");
    }
    
    @FXML
    void handleCoursButton(ActionEvent event) {
        // Navigate to cours page when implemented
        // navigateTo("/Controllers/cours.fxml");
    }
    
    @FXML
    void handleLogoutButton(ActionEvent event) {
        // Implement logout functionality
        // For now, just navigate to login page
        navigateTo("/Controllers/login.fxml");
    }
    
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
        }
    }
}