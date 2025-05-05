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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class add_module_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField nom_module;
    @FXML private TextArea description_module;
    @FXML private TextField module_img;
    @FXML private Button chooseImageBtn;
    @FXML private ImageView moduleImageView;

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
            selectedImagePath = m.getModule_img();
            
            // Load the existing image
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    File imageFile = new File("src/main/resources/images/modules/" + selectedImagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        moduleImageView.setImage(image);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            try {
                // Create a preview of the image
                Image image = new Image(selectedFile.toURI().toString());
                moduleImageView.setImage(image);
                
                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                selectedImagePath = fileName;
                module_img.setText(fileName);

                // Copy image to application's storage
                String storageDir = "src/main/resources/images/modules/";
                new File(storageDir).mkdirs();
                
                Path destination = Path.of(storageDir + fileName);
                Files.copy(Path.of(selectedFile.getAbsolutePath()), destination, 
                    StandardCopyOption.REPLACE_EXISTING);
                
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving image: " + e.getMessage());
            }
        }
    }

    @FXML
    void ajoutermodule(ActionEvent event) {
        String name = nom_module.getText();
        String description = description_module.getText();
        String imagePath = module_img.getText();

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

        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            if (isEditMode && moduleToEdit != null) {
                String sql = "UPDATE module SET nom_module=?, description_module=?, module_img=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, description);
                pst.setString(3, imagePath);
                pst.setInt(4, moduleToEdit.getId());
                pst.executeUpdate();

                showAlert("Module modifié avec succès !");
            } else {
                String sql = "INSERT INTO module (nom_module, description_module, module_img) VALUES (?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, description);
                pst.setString(3, imagePath);
                pst.executeUpdate();

                showAlert("Module ajouté avec succès !");
            }

            if (onSaveCallback != null) onSaveCallback.run();

<<<<<<< HEAD
       
=======
>>>>>>> wael
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
        selectedImagePath = null;
    }
    
    @FXML
    void exitForm(ActionEvent event) {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}