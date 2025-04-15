package com.eduhive.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.eduhive.entity.Annonce;
import com.eduhive.service.AnnonceService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnnonceController implements Initializable {
    @FXML private FlowPane cardsContainer;
    @FXML private VBox formPopup;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> categorieField;
    @FXML private Button addButton;
    @FXML private Button closeFormButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final AnnonceService annonceService = new AnnonceService();
    private Annonce currentAnnonce = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize ComboBox with categories
        categorieField.setItems(FXCollections.observableArrayList(
            "Administratif",
            "Academique",
            "Hors du programme"
        ));
        
        addButton.setOnAction(e -> showForm(null));
        closeFormButton.setOnAction(e -> hideForm());
        cancelButton.setOnAction(e -> hideForm());
        saveButton.setOnAction(e -> handleSave());
        
        // Add listeners for real-time validation
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateField(titreField, validateTitre(newVal)));
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> validateField(descriptionArea, validateDescription(newVal)));
        categorieField.valueProperty().addListener((obs, oldVal, newVal) -> validateField(categorieField, validateCategorie(newVal)));
        
        loadAnnonces();
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
        
        if (!validateDescription(descriptionArea.getText())) {
            isValid = false;
            errorMessage.append("- La description doit contenir entre 10 et 500 caractères\n");
            setErrorStyle(descriptionArea);
        }
        
        if (!validateCategorie(categorieField.getValue())) {
            isValid = false;
            errorMessage.append("- Veuillez sélectionner une catégorie\n");
            setErrorStyle(categorieField);
        }
        
        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return;
        }

        try {
            if (currentAnnonce == null) {
                // Create new annonce
                Annonce annonce = new Annonce(null,
                    titreField.getText().trim(),
                    descriptionArea.getText().trim(),
                    categorieField.getValue()
                );
                annonceService.create(annonce);
            } else {
                // Update existing annonce
                Annonce annonce = new Annonce(
                    currentAnnonce.getId(),
                    titreField.getText().trim(),
                    descriptionArea.getText().trim(),
                    categorieField.getValue()
                );
                annonceService.update(annonce);
            }
            hideForm();
            loadAnnonces();
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                currentAnnonce == null ? "Annonce ajoutée avec succès!" : "Annonce modifiée avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de l'opération: " + e.getMessage());
        }
    }

    private boolean validateTitre(String titre) {
        return titre != null && titre.trim().length() >= 3 && titre.trim().length() <= 50;
    }

    private boolean validateDescription(String description) {
        return description != null && description.trim().length() >= 10 && description.trim().length() <= 500;
    }

    private boolean validateCategorie(String categorie) {
        return categorie != null && !categorie.isEmpty();
    }

    private void validateField(Control field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2196F3;");
        } else {
            setErrorStyle(field);
        }
    }

    private void setErrorStyle(Control field) {
        field.setStyle("-fx-border-color: #f44336;");
    }

    private void resetValidationStyles() {
        titreField.setStyle("");
        descriptionArea.setStyle("");
        categorieField.setStyle("");
    }

    private void showForm(Annonce annonce) {
        currentAnnonce = annonce;
        if (annonce != null) {
            titreField.setText(annonce.getTitre());
            descriptionArea.setText(annonce.getDescription());
            categorieField.setValue(annonce.getCategorie());
        } else {
            clearFields();
        }
        formPopup.setVisible(true);
    }

    private void hideForm() {
        formPopup.setVisible(false);
        clearFields();
        currentAnnonce = null;
    }

    private void handleDelete(Annonce annonce) {
        try {
            annonceService.delete(annonce.getId());
            loadAnnonces();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Annonce supprimée avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void loadAnnonces() {
        try {
            cardsContainer.getChildren().clear();
            for (Annonce annonce : annonceService.readAll()) {
                cardsContainer.getChildren().add(createAnnonceCard(annonce));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des annonces: " + e.getMessage());
        }
    }

    private Node createAnnonceCard(Annonce annonce) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);

        Label titre = new Label(annonce.getTitre());
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        titre.setWrapText(true);

        Label description = new Label(annonce.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #666;");

        Label categorie = new Label("Catégorie: " + annonce.getCategorie());
        categorie.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        HBox buttons = new HBox(10);
        buttons.setStyle("-fx-padding: 10 0 0 0;");

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editButton.setOnAction(e -> showForm(annonce));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDelete(annonce));

        buttons.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(titre, description, categorie, buttons);
        return card;
    }

    private void clearFields() {
        titreField.clear();
        descriptionArea.clear();
        categorieField.setValue(null);
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