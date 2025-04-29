package com.eduhive.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.eduhive.entity.Stage;
import com.eduhive.service.StageService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class StageController implements Initializable {
    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private VBox formPopup;
    @FXML private TextField titreField;
    @FXML private TextField entrepriseField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField dureeField;
    @FXML private Button addButton;
    @FXML private Button closeFormButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final StageService stageService = new StageService();
    private Stage currentStage = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addButton.setOnAction(e -> showForm(null));
        closeFormButton.setOnAction(e -> hideForm());
        cancelButton.setOnAction(e -> hideForm());
        saveButton.setOnAction(e -> handleSave());
        
        // Add search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadStages();
        });
        
        // Add listeners for real-time validation
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateField(titreField, validateTitre(newVal)));
        entrepriseField.textProperty().addListener((obs, oldVal, newVal) -> validateField(entrepriseField, validateEntreprise(newVal)));
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> validateField(descriptionArea, validateDescription(newVal)));
        dureeField.textProperty().addListener((obs, oldVal, newVal) -> validateField(dureeField, validateDuree(newVal)));
        
        loadStages();
    }

    private void handleSave() {
        // Reset styles
        resetValidationStyles();
        
        // Validate all fields
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes:\n");
        
        if (!validateTitre(titreField.getText())) {
            isValid = false;
            errorMessage.append("- Le titre doit contenir entre 3 et 50 caractères\n");
            setErrorStyle(titreField);
        }
        
        if (!validateEntreprise(entrepriseField.getText())) {
            isValid = false;
            errorMessage.append("- L'entreprise doit contenir entre 3 et 50 caractères\n");
            setErrorStyle(entrepriseField);
        }
        
        if (!validateDescription(descriptionArea.getText())) {
            isValid = false;
            errorMessage.append("- La description doit contenir entre 10 et 500 caractères\n");
            setErrorStyle(descriptionArea);
        }
        
        if (!validateDuree(dureeField.getText())) {
            isValid = false;
            errorMessage.append("- La durée doit contenir entre 3 et 30 caractères\n");
            setErrorStyle(dureeField);
        }
        
        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return;
        }

        try {
            Stage stage = new Stage();
            if (currentStage != null) {
                stage.setId(currentStage.getId());
            }
            stage.setTitre(titreField.getText().trim());
            stage.setEntreprise(entrepriseField.getText().trim());
            stage.setDescription(descriptionArea.getText().trim());
            stage.setDuree(dureeField.getText().trim());

            if (currentStage == null) {
                stageService.create(stage);
            } else {
                stageService.update(stage);
            }
            hideForm();
            loadStages();
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                currentStage == null ? "Stage ajouté avec succès!" : "Stage modifié avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de l'opération: " + e.getMessage());
        }
    }

    private boolean validateTitre(String titre) {
        return titre != null && titre.trim().length() >= 3 && titre.trim().length() <= 50;
    }

    private boolean validateEntreprise(String entreprise) {
        return entreprise != null && entreprise.trim().length() >= 3 && entreprise.trim().length() <= 50;
    }

    private boolean validateDescription(String description) {
        return description != null && description.trim().length() >= 10 && description.trim().length() <= 500;
    }

    private boolean validateDuree(String duree) {
        return duree != null && duree.trim().length() >= 3 && duree.trim().length() <= 30;
    }

    private void validateField(TextInputControl field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2196F3;");
        } else {
            setErrorStyle(field);
        }
    }

    private void setErrorStyle(TextInputControl field) {
        field.setStyle("-fx-border-color: #f44336;");
    }

    private void resetValidationStyles() {
        titreField.setStyle("");
        entrepriseField.setStyle("");
        descriptionArea.setStyle("");
        dureeField.setStyle("");
    }

    private void showForm(Stage stage) {
        currentStage = stage;
        if (stage != null) {
            titreField.setText(stage.getTitre());
            entrepriseField.setText(stage.getEntreprise());
            descriptionArea.setText(stage.getDescription());
            dureeField.setText(stage.getDuree());
        } else {
            clearFields();
        }
        formPopup.setVisible(true);
    }

    private void hideForm() {
        formPopup.setVisible(false);
        clearFields();
        currentStage = null;
    }

    private void handleDelete(Stage stage) {
        try {
            stageService.delete(stage.getId());
            loadStages();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Stage supprimé avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void loadStages() {
        try {
            cardsContainer.getChildren().clear();
            String searchText = searchField.getText().toLowerCase();
            
            stageService.readAll().stream()
                .filter(stage -> 
                    searchText == null || 
                    searchText.isEmpty() || 
                    stage.getEntreprise().toLowerCase().contains(searchText)
                )
                .forEach(stage -> {
                    Node card = createStageCard(stage);
                    cardsContainer.getChildren().add(card);
                });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des stages: " + e.getMessage());
        }
    }

    private WritableImage generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            WritableImage qrImage = new WritableImage(width, height);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrImage.getPixelWriter().setColor(x, y, 
                        bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return qrImage;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Node createStageCard(Stage stage) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);

        Label titre = new Label(stage.getTitre());
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        titre.setWrapText(true);

        Label entreprise = new Label("Entreprise: " + stage.getEntreprise());
        entreprise.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        entreprise.setWrapText(true);

        Label description = new Label(stage.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #666;");

        Label duree = new Label("Durée: " + stage.getDuree());
        duree.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        HBox buttons = new HBox(10);
        buttons.setStyle("-fx-padding: 10 0 0 0;");

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editButton.setOnAction(e -> showForm(stage));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDelete(stage));

        buttons.getChildren().addAll(editButton, deleteButton);

        // Generate and add QR code
        String email = stage.getEntreprise().toLowerCase() + "@gmail.com";
        WritableImage qrCode = generateQRCode(email, 100, 100);
        if (qrCode != null) {
            ImageView qrView = new ImageView(qrCode);
            qrView.setFitWidth(100);
            qrView.setFitHeight(100);
            
            VBox qrContainer = new VBox(5);
            qrContainer.setStyle("-fx-padding: 10 0 0 0; -fx-alignment: center;");
            qrContainer.getChildren().add(qrView);
            
            card.getChildren().addAll(titre, entreprise, description, duree, buttons, qrContainer);
        } else {
            card.getChildren().addAll(titre, entreprise, description, duree, buttons);
        }

        return card;
    }

    private void clearFields() {
        titreField.clear();
        entrepriseField.clear();
        descriptionArea.clear();
        dureeField.clear();
        resetValidationStyles();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 