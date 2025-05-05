package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import Entities.quiz;
import Services.QuizService;
import Services.QuizServiceImpl;
import javafx.stage.Stage;

public class add_quiz_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField option_a;
    @FXML private TextField option_b;
    @FXML private TextField question;
    @FXML private TextField rep_correct;
    @FXML private TextField titre;

    private boolean isEditMode = false;
    private quiz quizToEdit;
    private Runnable onSaveCallback; 
    
    private QuizService quizService = new QuizServiceImpl();

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(quiz q) {
        if (q != null) {
            this.quizToEdit = q;
            this.isEditMode = true;
            ajouter.setText("Modifier");

            titre.setText(q.getTitre());
            question.setText(q.getQuestion());
            rep_correct.setText(q.getRepCorrect());
            option_a.setText(q.getOptionA());
            option_b.setText(q.getOptionB());
        }
    }

    @FXML
    void ajouterquiz(ActionEvent event) {
        String t = titre.getText();
        String q = question.getText();
        String r = rep_correct.getText();
        String a = option_a.getText();
        String b = option_b.getText();

        try {
            if (isEditMode && quizToEdit != null) {

                quiz updatedQuiz = new quiz(
                    quizToEdit.getId(),
                    t,
                    q,
                    r,
                    a,
                    b
                );
                quizService.updateQuiz(updatedQuiz);
                showAlert("Quiz modifié avec succès !");
            } else {

                quiz newQuiz = new quiz(
                    0,
                    t,
                    q,
                    r,
                    a,
                    b
                );
                quizService.addQuiz(newQuiz);
                showAlert("Quiz ajouté avec succès !");
            }

            if (onSaveCallback != null) onSaveCallback.run();


            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    void effacerquiz(ActionEvent event) {
        titre.clear();
        question.clear();
        rep_correct.clear();
        option_a.clear();
        option_b.clear();
    }
}
