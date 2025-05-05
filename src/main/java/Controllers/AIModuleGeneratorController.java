package Controllers;

import Entities.Module;
import Services.ModuleService;
import Services.ModuleServiceImpl;
import Services.GeminiAIService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AIModuleGeneratorController {
    @FXML
    private TextField moduleNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ImageView moduleImageView;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveModuleButton;

    @FXML
    private Button chooseImageButton;

    @FXML
    private Label loadingLabel;

    private String selectedImagePath;
    private final GeminiAIService geminiAIService = new GeminiAIService();
    private final ModuleService moduleService = new ModuleServiceImpl();

    @FXML
    void initialize() {
        // Initially disable the save button until we have content
        saveModuleButton.setDisable(true);
        
        // Enable save button when both name and description are filled
        moduleNameField.textProperty().addListener((obs, old, newValue) -> 
            updateSaveButtonState());
        
        descriptionArea.textProperty().addListener((obs, old, newValue) -> 
            updateSaveButtonState());
    }

    private void updateSaveButtonState() {
        boolean hasContent = !moduleNameField.getText().trim().isEmpty() 
                && !descriptionArea.getText().trim().isEmpty();
        saveModuleButton.setDisable(!hasContent);
    }

    @FXML
    void handleGenerate(ActionEvent event) {
        String moduleName = moduleNameField.getText().trim();
        if (moduleName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un nom de module.");
            return;
        }

        generateButton.setDisable(true);
        loadingLabel.setVisible(true);

        Task<String> generateTask = new Task<>() {
            @Override
            protected String call() {
                String prompt = String.format(
                    "Générer une description détaillée et professionnelle pour un module d'enseignement nommé '%s'. " +
                    "La description doit inclure : " +
                    "1. Un aperçu général du module " +
                    "2. Les objectifs d'apprentissage principaux " +
                    "3. Les compétences qui seront développées " +
                    "Garder la description concise mais informative, environ 3-4 phrases.", 
                    moduleName
                );
                return geminiAIService.generateContent(prompt);
            }
        };

        generateTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                descriptionArea.setText(generateTask.getValue());
                generateButton.setDisable(false);
                loadingLabel.setVisible(false);
                updateSaveButtonState();
            });
        });

        generateTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la génération de la description : " + generateTask.getException().getMessage());
                generateButton.setDisable(false);
                loadingLabel.setVisible(false);
            });
        });

        new Thread(generateTask).start();
    }

    @FXML
    void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                moduleImageView.setImage(image);
                selectedImagePath = file.getAbsolutePath();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    void handleSaveModule(ActionEvent event) {
        String name = moduleNameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", 
                "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            String finalImagePath = null;
            if (selectedImagePath != null) {
                // Copy image to application's storage
                String storageDir = "src/main/resources/images/modules/";
                new File(storageDir).mkdirs();
                
                String fileName = System.currentTimeMillis() + "_" + 
                    new File(selectedImagePath).getName();
                Path destination = Path.of(storageDir + fileName);
                
                Files.copy(Path.of(selectedImagePath), destination, 
                    StandardCopyOption.REPLACE_EXISTING);
                finalImagePath = fileName;
            }

            Module module = new Module();
            module.setNom_module(name);
            module.setDescription_module(description);
            module.setModule_img(finalImagePath);

            moduleService.addModule(module);

            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Le module a été ajouté avec succès !");
            handleBack(event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de l'enregistrement du module : " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        ((Stage) moduleNameField.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
