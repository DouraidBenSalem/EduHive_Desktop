package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import Entities.Classe;
import utils.MyDatabase;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class add_classe_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField classename;
    @FXML private TextField num_etudiant;

    private boolean isEditMode = false;
    private Classe classeToEdit;
    private Runnable onSaveCallback; // pour rafraîchir automatiquement après ajout/modif

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(Classe c) {
        if (c != null) {
            this.classeToEdit = c;
            this.isEditMode = true;
            ajouter.setText("Modifier");

            classename.setText(c.getClassename());
            num_etudiant.setText(String.valueOf(c.getNum_etudiant()));
        }
    }

    @FXML
    void ajouterclasse(ActionEvent event) {
        String name = classename.getText();

        if (name == null || name.trim().isEmpty()) {
            showError("Le nom de la classe ne doit pas être vide.");
            return;
        }

        int numEtudiant;
        try {
            numEtudiant = Integer.parseInt(num_etudiant.getText());
        } catch (NumberFormatException e) {
            showError("Le nombre d'étudiants doit être un nombre entier.");
            return;
        }

        if (numEtudiant <= 10 || numEtudiant >= 40) {
            showError("Le nombre d'étudiants doit être supérieur à 10 et inférieur à 40.");
            return;
        }

        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            if (isEditMode && classeToEdit != null) {
                String sql = "UPDATE classe SET classename=?, num_etudiant=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setInt(2, numEtudiant);
                pst.setInt(3, classeToEdit.getId());
                pst.executeUpdate();

                showAlert("Classe modifiée avec succès !");
            } else {
                String sql = "INSERT INTO classe (classename, num_etudiant) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setInt(2, numEtudiant);
                pst.executeUpdate();

                showAlert("Classe ajoutée avec succès !");
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
    void effacerclasse(ActionEvent event) {
        classename.clear();
        num_etudiant.clear();
    }
}