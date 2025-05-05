package Controllers;

import Entities.Result;
import Entities.quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Services.QuizService;
import Services.QuizServiceImpl;
import Services.ResultService;
import Services.ResultServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import utils.MyDatabase;

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

    private Map<String, Integer> userMap = new HashMap<>();
    private Map<String, Integer> quizMap = new HashMap<>();

    @FXML
    private Label noteErrorLabel;

    @FXML
    private Label repCorrectErrorLabel;

    @FXML
    private Label repIncorrectErrorLabel;

    @FXML
    private Label userErrorLabel;

    @FXML
    private Label commentaireErrorLabel;

    @FXML
    private Label quizErrorLabel;

    @FXML
    void initialize() {
        loadUsers();
        loadQuizzes();

        // Add numeric validation to fields with real-time error feedback
        noteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                noteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateNoteField();
        });

        nbRepCorrectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbRepCorrectField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateRepCorrectField();
        });

        nbRepIncorrectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbRepIncorrectField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateRepIncorrectField();
        });

        // Add validation for quiz selection
        quizComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateQuizSelection();
        });
    }

    private boolean validateNoteField() {
        if (noteField.getText().isEmpty()) {
            noteErrorLabel.setText("La note est obligatoire");
            noteErrorLabel.setVisible(true);
            noteErrorLabel.setManaged(true);
            return false;
        }

        try {
            int note = Integer.parseInt(noteField.getText());
            if (note < 0 || note > 20) {
                noteErrorLabel.setText("La note doit être entre 0 et 20");
                noteErrorLabel.setVisible(true);
                noteErrorLabel.setManaged(true);
                return false;
            }
        } catch (NumberFormatException e) {
            noteErrorLabel.setText("Veuillez entrer un nombre valide");
            noteErrorLabel.setVisible(true);
            noteErrorLabel.setManaged(true);
            return false;
        }

        noteErrorLabel.setVisible(false);
        noteErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateRepCorrectField() {
        if (nbRepCorrectField.getText().isEmpty()) {
            repCorrectErrorLabel.setText("Le nombre de réponses correctes est obligatoire");
            repCorrectErrorLabel.setVisible(true);
            repCorrectErrorLabel.setManaged(true);
            return false;
        }

        try {
            int repCorrect = Integer.parseInt(nbRepCorrectField.getText());
            if (repCorrect < 0) {
                repCorrectErrorLabel.setText("Le nombre ne peut pas être négatif");
                repCorrectErrorLabel.setVisible(true);
                repCorrectErrorLabel.setManaged(true);
                return false;
            }
        } catch (NumberFormatException e) {
            repCorrectErrorLabel.setText("Veuillez entrer un nombre valide");
            repCorrectErrorLabel.setVisible(true);
            repCorrectErrorLabel.setManaged(true);
            return false;
        }

        repCorrectErrorLabel.setVisible(false);
        repCorrectErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateRepIncorrectField() {
        if (nbRepIncorrectField.getText().isEmpty()) {
            repIncorrectErrorLabel.setText("Le nombre de réponses incorrectes est obligatoire");
            repIncorrectErrorLabel.setVisible(true);
            repIncorrectErrorLabel.setManaged(true);
            return false;
        }

        try {
            int repIncorrect = Integer.parseInt(nbRepIncorrectField.getText());
            if (repIncorrect < 0) {
                repIncorrectErrorLabel.setText("Le nombre ne peut pas être négatif");
                repIncorrectErrorLabel.setVisible(true);
                repIncorrectErrorLabel.setManaged(true);
                return false;
            }
        } catch (NumberFormatException e) {
            repIncorrectErrorLabel.setText("Veuillez entrer un nombre valide");
            repIncorrectErrorLabel.setVisible(true);
            repIncorrectErrorLabel.setManaged(true);
            return false;
        }

        repIncorrectErrorLabel.setVisible(false);
        repIncorrectErrorLabel.setManaged(false);
        return true;
    }

    private void loadUsers() {
        ObservableList<String> userOptions = FXCollections.observableArrayList();

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
        quizOptions.add("None");

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


        for (String userOption : userComboBox.getItems()) {
            if (userMap.get(userOption) == result.getUserId()) {
                userComboBox.setValue(userOption);
                break;
            }
        }


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


            String selectedUser = userComboBox.getValue();
            if (selectedUser == null) {
                return;
            }
            int userId = userMap.get(selectedUser);
            String userName = selectedUser.substring(selectedUser.indexOf(" - ") + 3); // Extract name


            Integer quizId = null;
            String quizTitle = null;
            String selectedQuiz = quizComboBox.getValue();
            if (selectedQuiz != null && !selectedQuiz.equals("None")) {
                quizId = quizMap.get(selectedQuiz);
                quizTitle = selectedQuiz.substring(selectedQuiz.indexOf(" - ") + 3); // Extract title
            }

            if (isEditMode) {

                currentResult.setNote(note);
                currentResult.setCommentaire(commentaire);
                currentResult.setNbRepCorrect(nbRepCorrect);
                currentResult.setNbRepIncorrect(nbRepIncorrect);
                currentResult.setUserId(userId);
                currentResult.setUserName(userName);
                currentResult.setQuizId(quizId);
                currentResult.setQuizTitle(quizTitle);

                resultService.updateResult(currentResult);

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
                // Succès sans alerte
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeStage();
        } catch (NumberFormatException e) {
            // Les validations de champs numériques sont déjà gérées par les méthodes de validation
        }
    }

    private boolean validateQuizSelection() {
        String selectedQuiz = quizComboBox.getValue();
        if (selectedQuiz == null) {
            if (quizErrorLabel != null) {
                quizErrorLabel.setText("Veuillez sélectionner un quiz");
                quizErrorLabel.setVisible(true);
                quizErrorLabel.setManaged(true);
            }
            return false;
        } else if (quizErrorLabel != null) {
            quizErrorLabel.setVisible(false);
            quizErrorLabel.setManaged(false);
        }
        return true;
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate note field
        if (!validateNoteField()) {
            isValid = false;
        }

        // Validate nbRepCorrect field
        if (!validateRepCorrectField()) {
            isValid = false;
        }

        // Validate nbRepIncorrect field
        if (!validateRepIncorrectField()) {
            isValid = false;
        }

        // Validate user selection
        if (userComboBox.getValue() == null) {
            if (userErrorLabel != null) {
                userErrorLabel.setText("Veuillez sélectionner un utilisateur");
                userErrorLabel.setVisible(true);
                userErrorLabel.setManaged(true);
            }
            isValid = false;
        } else if (userErrorLabel != null) {
            userErrorLabel.setVisible(false);
            userErrorLabel.setManaged(false);
        }

        // Validate quiz selection
        if (!validateQuizSelection()) {
            isValid = false;
        }

        // Validate commentaire
        if (commentaireArea.getText().isEmpty()) {
            if (commentaireErrorLabel != null) {
                commentaireErrorLabel.setText("Le commentaire est obligatoire");
                commentaireErrorLabel.setVisible(true);
                commentaireErrorLabel.setManaged(true);
            }
            isValid = false;
        } else if (commentaireErrorLabel != null) {
            commentaireErrorLabel.setVisible(false);
            commentaireErrorLabel.setManaged(false);
        }

        return isValid;
    }



    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}