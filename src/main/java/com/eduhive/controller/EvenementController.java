package com.eduhive.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import com.eduhive.entity.Evenement;
import com.eduhive.service.EvenementService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EvenementController implements Initializable {
    @FXML private FlowPane cardsContainer;
    @FXML private VBox formPopup;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField lieuField;
    @FXML private TextField dateField;
    @FXML private TextField organisateurField;
    @FXML private Button addButton;
    @FXML private Button closeFormButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final EvenementService evenementService = new EvenementService();
    private Evenement currentEvenement = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addButton.setOnAction(e -> showForm(null));
        closeFormButton.setOnAction(e -> hideForm());
        cancelButton.setOnAction(e -> hideForm());
        saveButton.setOnAction(e -> handleSave());
        
        // Add listeners for real-time validation
        nomField.textProperty().addListener((obs, oldVal, newVal) -> validateField(nomField, validateNom(newVal)));
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> validateField(descriptionArea, validateDescription(newVal)));
        lieuField.textProperty().addListener((obs, oldVal, newVal) -> validateField(lieuField, validateLieu(newVal)));
        dateField.textProperty().addListener((obs, oldVal, newVal) -> validateField(dateField, validateDate(newVal)));
        organisateurField.textProperty().addListener((obs, oldVal, newVal) -> validateField(organisateurField, validateOrganisateur(newVal)));
        
        loadEvenements();
    }

    private void showForm(Evenement evenement) {
        currentEvenement = evenement;
        if (evenement != null) {
            nomField.setText(evenement.getNom());
            descriptionArea.setText(evenement.getDescription());
            lieuField.setText(evenement.getLieu());
            dateField.setText(evenement.getDate());
            organisateurField.setText(evenement.getOrganisateur());
        } else {
            clearFields();
        }
        formPopup.setVisible(true);
    }

    private void hideForm() {
        formPopup.setVisible(false);
        clearFields();
        currentEvenement = null;
    }

    private void handleSave() {
        // Reset styles
        resetValidationStyles();
        
        // Validate all fields
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes:\n");
        
        if (!validateNom(nomField.getText())) {
            isValid = false;
            errorMessage.append("- Le nom doit contenir entre 3 et 50 caractères\n");
            setErrorStyle(nomField);
        }
        
        if (!validateDescription(descriptionArea.getText())) {
            isValid = false;
            errorMessage.append("- La description doit contenir entre 10 et 500 caractères\n");
            setErrorStyle(descriptionArea);
        }
        
        if (!validateLieu(lieuField.getText())) {
            isValid = false;
            errorMessage.append("- Le lieu doit contenir entre 3 et 100 caractères\n");
            setErrorStyle(lieuField);
        }
        
        if (!validateDate(dateField.getText())) {
            isValid = false;
            errorMessage.append("- La date doit être au format AAAA-MM-JJ et ne peut pas être dans le passé\n");
            setErrorStyle(dateField);
        }
        
        if (!validateOrganisateur(organisateurField.getText())) {
            isValid = false;
            errorMessage.append("- L'organisateur doit contenir entre 3 et 50 caractères\n");
            setErrorStyle(organisateurField);
        }
        
        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return;
        }

        try {
            if (currentEvenement == null) {
                // Create new evenement
                Evenement evenement = Evenement.builder()
                    .nom(nomField.getText().trim())
                    .description(descriptionArea.getText().trim())
                    .lieu(lieuField.getText().trim())
                    .date(dateField.getText().trim())
                    .organisateur(organisateurField.getText().trim())
                    .build();
                evenementService.create(evenement);
            } else {
                // Update existing evenement
                Evenement evenement = Evenement.builder()
                    .id(currentEvenement.getId())
                    .nom(nomField.getText().trim())
                    .description(descriptionArea.getText().trim())
                    .lieu(lieuField.getText().trim())
                    .date(dateField.getText().trim())
                    .organisateur(organisateurField.getText().trim())
                    .build();
                evenementService.update(evenement);
            }
            hideForm();
            loadEvenements();
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                currentEvenement == null ? "Événement ajouté avec succès!" : "Événement modifié avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de l'opération: " + e.getMessage());
        }
    }

    private boolean validateNom(String nom) {
        return nom != null && nom.trim().length() >= 3 && nom.trim().length() <= 50;
    }

    private boolean validateDescription(String description) {
        return description != null && description.trim().length() >= 10 && description.trim().length() <= 500;
    }

    private boolean validateLieu(String lieu) {
        return lieu != null && lieu.trim().length() >= 3 && lieu.trim().length() <= 100;
    }

    private boolean validateDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate eventDate = LocalDate.parse(date.trim(), formatter);
            return !eventDate.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean validateOrganisateur(String organisateur) {
        return organisateur != null && organisateur.trim().length() >= 3 && organisateur.trim().length() <= 50;
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
        nomField.setStyle("");
        descriptionArea.setStyle("");
        lieuField.setStyle("");
        dateField.setStyle("");
        organisateurField.setStyle("");
    }

    private void handleDelete(Evenement evenement) {
        try {
            evenementService.delete(evenement.getId());
            loadEvenements();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé avec succès!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void loadEvenements() {
        try {
            cardsContainer.getChildren().clear();
            for (Evenement evenement : evenementService.readAll()) {
                cardsContainer.getChildren().add(createEvenementCard(evenement));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    private Node createEvenementCard(Evenement evenement) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);

        Label nom = new Label(evenement.getNom());
        nom.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        nom.setWrapText(true);

        Label description = new Label(evenement.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #666;");

        Label lieu = new Label("Lieu: " + evenement.getLieu());
        lieu.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        lieu.setWrapText(true);

        Label date = new Label("Date: " + evenement.getDate());
        date.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        Label organisateur = new Label("Organisateur: " + evenement.getOrganisateur());
        organisateur.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        HBox buttons = new HBox(10);
        buttons.setStyle("-fx-padding: 10 0 0 0;");

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editButton.setOnAction(e -> showForm(evenement));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDelete(evenement));

        buttons.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nom, description, lieu, date, organisateur, buttons);
        return card;
    }

    private void clearFields() {
        nomField.clear();
        descriptionArea.clear();
        lieuField.clear();
        dateField.clear();
        organisateurField.clear();
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