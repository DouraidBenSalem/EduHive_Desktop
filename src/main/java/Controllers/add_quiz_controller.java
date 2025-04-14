package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import Entities.quiz;
import utils.MyDatabase;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;


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
    private Runnable onSaveCallback; // pour rafraîchir automatiquement après ajout/modif

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
            Connection conn = MyDatabase.getInstance().getConnection();

            if (isEditMode && quizToEdit != null) {
                String sql = "UPDATE quiz SET titre=?, question=?, rep_correct=?, option_a=?, option_b=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, t);
                pst.setString(2, q);
                pst.setString(3, r);
                pst.setString(4, a);
                pst.setString(5, b);
                pst.setInt(6, quizToEdit.getId());
                pst.executeUpdate();

                showAlert("Quiz modifié avec succès !");
            } else {
                String sql = "INSERT INTO quiz (titre, question, rep_correct, option_a, option_b) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, t);
                pst.setString(2, q);
                pst.setString(3, r);
                pst.setString(4, a);
                pst.setString(5, b);
                pst.executeUpdate();

                showAlert("Quiz ajouté avec succès !");
            }

            if (onSaveCallback != null) onSaveCallback.run();

            // Fermer la fenêtre
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
