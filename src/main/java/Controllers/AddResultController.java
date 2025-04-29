package Controllers;

import Entities.Result;
import Entities.quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.QuizService;
import services.QuizServiceImpl;
import services.ResultService;
import services.ResultServiceImpl;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AddResultController {

    @FXML
    private TextField noteField;

    @FXML
    private TextArea commentaireArea;

    @FXML
    private TextField nbRepCorrectField;

    @FXML
    private TextField nbRepIncorrectField;

    @FXML
    private ComboBox<String> userComboBox;

    @FXML
    private ComboBox<String> quizComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private ResultService resultService = new ResultServiceImpl();
    private QuizService quizService = new QuizServiceImpl();
    
    private Result currentResult;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;
    
    // Maps to store ID-Name mappings
    private Map<String, Integer> userMap = new HashMap<>();
    private Map<String, Integer> quizMap = new HashMap<>();

    @FXML
    void initialize() {
        loadUsers();
        loadQuizzes();
        

        noteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                noteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        nbRepCorrectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbRepCorrectField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        nbRepIncorrectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbRepIncorrectField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadUsers() {
        ObservableList<String> userOptions = FXCollections.observableArrayList();
        // Fix the database connection method call
        Connection conn = MyDatabase.getInstance().getConnection();
        
        try {
            String query = "SELECT id, nom FROM user";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("nom");
                String displayText = id + " - " + name;
                userOptions.add(displayText);
                userMap.put(displayText, id);
            }
            
            userComboBox.setItems(userOptions);
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadQuizzes() {
        ObservableList<String> quizOptions = FXCollections.observableArrayList();
        quizOptions.add("None"); // Option for null quiz_id
        
        for (quiz q : quizService.getAllQuizzes()) {
            String displayText = q.getId() + " - " + q.getTitre();
            quizOptions.add(displayText);
            quizMap.put(displayText, q.getId());
        }
        
        quizComboBox.setItems(quizOptions);
    }

    public void initData(Result result) {
        this.currentResult = result;
        this.isEditMode = true;
        
        noteField.setText(String.valueOf(result.getNote()));
        commentaireArea.setText(result.getCommentaire());
        nbRepCorrectField.setText(String.valueOf(result.getNbRepCorrect()));
        nbRepIncorrectField.setText(String.valueOf(result.getNbRepIncorrect()));
        
        // Set user selection
        for (String userOption : userComboBox.getItems()) {
            if (userMap.get(userOption) == result.getUserId()) {
                userComboBox.setValue(userOption);
                break;
            }
        }
        
        // Set quiz selection
        if (result.getQuizId() == null) {
            quizComboBox.setValue("None");
        } else {
            for (String quizOption : quizComboBox.getItems()) {
                if (quizOption.equals("None")) continue;
                if (quizMap.get(quizOption) == result.getQuizId()) {
                    quizComboBox.setValue(quizOption);
                    break;
                }
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeStage();
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        
        try {
            int note = Integer.parseInt(noteField.getText());
            String commentaire = commentaireArea.getText();
            int nbRepCorrect = Integer.parseInt(nbRepCorrectField.getText());
            int nbRepIncorrect = Integer.parseInt(nbRepIncorrectField.getText());
            
            // Get selected user ID and name
            String selectedUser = userComboBox.getValue();
            if (selectedUser == null) {
                showAlert("Erreur", "Veuillez sélectionner un utilisateur.");
                return;
            }
            int userId = userMap.get(selectedUser);
            String userName = selectedUser.substring(selectedUser.indexOf(" - ") + 3); // Extract name
            
            // Get selected quiz ID and title (can be null)
            Integer quizId = null;
            String quizTitle = null;
            String selectedQuiz = quizComboBox.getValue();
            if (selectedQuiz != null && !selectedQuiz.equals("None")) {
                quizId = quizMap.get(selectedQuiz);
                quizTitle = selectedQuiz.substring(selectedQuiz.indexOf(" - ") + 3); // Extract title
            }
            
            if (isEditMode) {
                // Update existing result
                currentResult.setNote(note);
                currentResult.setCommentaire(commentaire);
                currentResult.setNbRepCorrect(nbRepCorrect);
                currentResult.setNbRepIncorrect(nbRepIncorrect);
                currentResult.setUserId(userId);
                currentResult.setUserName(userName);
                currentResult.setQuizId(quizId);
                currentResult.setQuizTitle(quizTitle);
                
                resultService.updateResult(currentResult);
                showAlert("Succès", "Résultat mis à jour avec succès!");
            } else {
                // Create new result
                Result newResult = new Result();
                newResult.setUserId(userId);
                newResult.setUserName(userName);
                newResult.setNote(note);
                newResult.setCommentaire(commentaire);
                newResult.setNbRepCorrect(nbRepCorrect);
                newResult.setNbRepIncorrect(nbRepIncorrect);
                newResult.setQuizId(quizId);
                newResult.setQuizTitle(quizTitle);
                
                resultService.addResult(newResult);
                showAlert("Succès", "Résultat ajouté avec succès!");
            }
            
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            closeStage();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides.");
        }
    }

    private boolean validateInputs() {
        if (noteField.getText().isEmpty() || 
            commentaireArea.getText().isEmpty() || 
            nbRepCorrectField.getText().isEmpty() || 
            nbRepIncorrectField.getText().isEmpty() ||
            userComboBox.getValue() == null) {
            
            showAlert("Erreur de validation", "Tous les champs sont obligatoires sauf le quiz.");
            return false;
        }
        
        try {
            int note = Integer.parseInt(noteField.getText());
            if (note < 0 || note > 20) {
                showAlert("Erreur de validation", "La note doit être entre 0 et 20.");
                return false;
            }
            
            int nbRepCorrect = Integer.parseInt(nbRepCorrectField.getText());
            int nbRepIncorrect = Integer.parseInt(nbRepIncorrectField.getText());
            
            if (nbRepCorrect < 0 || nbRepIncorrect < 0) {
                showAlert("Erreur de validation", "Le nombre de réponses ne peut pas être négatif.");
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Veuillez entrer des valeurs numériques valides.");
            return false;
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}