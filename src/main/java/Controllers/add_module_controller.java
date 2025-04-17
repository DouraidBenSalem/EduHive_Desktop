package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import Entities.Module;
import utils.MyDatabase;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class add_module_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField nom_module;
    @FXML private TextArea description_module;
    @FXML private TextField module_img;
    @FXML private TextField moy_module;
    @FXML private Button chooseImageBtn;

    private boolean isEditMode = false;
    private Module moduleToEdit;
    private Runnable onSaveCallback; 
    private String selectedImagePath;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(Module m) {
        if (m != null) {
            this.moduleToEdit = m;
            this.isEditMode = true;
            ajouter.setText("Modifier");

            nom_module.setText(m.getNom_module());
            description_module.setText(m.getDescription_module());
            module_img.setText(m.getModule_img());
            moy_module.setText(String.valueOf(m.getMoy()));
            selectedImagePath = m.getModule_img();
        }
    }

    @FXML
    void initialize() {
       
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Module Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(chooseImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getName();
            module_img.setText(selectedImagePath);
        }
    }

    @FXML
    void ajoutermodule(ActionEvent event) {
        String name = nom_module.getText();
        String description = description_module.getText();
        String imagePath = module_img.getText();
        String moyText = moy_module.getText();

        if (name == null || name.trim().isEmpty()) {
            showError("Le nom du module ne doit pas être vide.");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            showError("La description du module ne doit pas être vide.");
            return;
        }

        if (imagePath == null || imagePath.trim().isEmpty()) {
            showError("Vous devez insérer une image pour le module.");
            return;
        }

        if (moyText == null || moyText.trim().isEmpty()) {
            showError("La moyenne du module ne doit pas être vide.");
            return;
        }

        double moy;
        try {
            moy = Double.parseDouble(moyText);
        } catch (NumberFormatException e) {
            showError("La moyenne doit être un nombre valide.");
            return;
        }

        if (moy < 0 || moy > 20) {
            showError("La moyenne doit être comprise entre 0 et 20.");
            return;
        }

        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            if (isEditMode && moduleToEdit != null) {
                String sql = "UPDATE module SET nom_module=?, description_module=?, module_img=?, moy=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, description);
                pst.setString(3, imagePath);
                pst.setDouble(4, moy);
                pst.setInt(5, moduleToEdit.getId());
                pst.executeUpdate();

                showAlert("Module modifié avec succès !");
            } else {
                String sql = "INSERT INTO module (nom_module, description_module, module_img, moy) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, description);
                pst.setString(3, imagePath);
                pst.setDouble(4, moy);
                pst.executeUpdate();

                showAlert("Module ajouté avec succès !");
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
    void effacermodule(ActionEvent event) {
        nom_module.clear();
        description_module.clear();
        module_img.clear();
        moy_module.clear();
        selectedImagePath = null;
    }
    
    @FXML
    void exitForm(ActionEvent event) {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}