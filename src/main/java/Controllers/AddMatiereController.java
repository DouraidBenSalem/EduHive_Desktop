package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import Entities.Matiere;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddMatiereController {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField nomMatiere;
    @FXML
    private TextField descriptionMatiere;
    @FXML
    private TextField objectifMatiere;
    @FXML
    private TextField moduleId;
    @FXML
    private TextField enseignantId;
    @FXML
    private TextField prerequisMatiere;

    // Labels pour les messages d'erreur
    private Label nomError;
    private Label descriptionError;
    private Label objectifError;
    private Label moduleIdError;
    private Label enseignantIdError;
    private Label prerequisError;

    // Style CSS pour les champs valides et invalides
    private final String VALID_STYLE = "-fx-border-color: green; -fx-border-width: 2px;";
    private final String INVALID_STYLE = "-fx-border-color: red; -fx-border-width: 2px;";
    private final String NORMAL_STYLE = "-fx-border-color: lightgray; -fx-border-width: 1px;";

    // Tooltips pour les champs
    private Tooltip nomTooltip = new Tooltip("Entrez le nom de la matière");
    private Tooltip descriptionTooltip = new Tooltip("Entrez une description pour la matière");
    private Tooltip objectifTooltip = new Tooltip("Entrez les objectifs de la matière");
    private Tooltip moduleIdTooltip = new Tooltip("Entrez l'ID du module (nombre entier)");
    private Tooltip enseignantIdTooltip = new Tooltip("Entrez l'ID de l'enseignant (nombre entier)");
    private Tooltip prerequisTooltip = new Tooltip("Entrez l'ID du prérequis (nombre entier, optionnel)");

    private boolean isEditMode = false;
    private Matiere matiereToEdit;
    private Runnable onSaveCallback;

    private MatiereService matiereService = new MatiereServiceImpl();

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        // Initialiser les labels d'erreur
        setupErrorLabels();

        // Configurer les tooltips
        nomMatiere.setTooltip(nomTooltip);
        descriptionMatiere.setTooltip(descriptionTooltip);
        objectifMatiere.setTooltip(objectifTooltip);
        moduleId.setTooltip(moduleIdTooltip);
        enseignantId.setTooltip(enseignantIdTooltip);
        prerequisMatiere.setTooltip(prerequisTooltip);

        // Ajouter les écouteurs pour la validation en temps réel
        setupValidationListeners();
    }

    private void setupErrorLabels() {
        // Créer les labels d'erreur
        nomError = new Label();
        nomError.setTextFill(Color.RED);
        nomError.setVisible(false);

        descriptionError = new Label();
        descriptionError.setTextFill(Color.RED);
        descriptionError.setVisible(false);

        objectifError = new Label();
        objectifError.setTextFill(Color.RED);
        objectifError.setVisible(false);

        moduleIdError = new Label();
        moduleIdError.setTextFill(Color.RED);
        moduleIdError.setVisible(false);

        enseignantIdError = new Label();
        enseignantIdError.setTextFill(Color.RED);
        enseignantIdError.setVisible(false);

        prerequisError = new Label();
        prerequisError.setTextFill(Color.RED);
        prerequisError.setVisible(false);

        // Ajouter les labels d'erreur dans le même HBox parent que chaque champ
        ((HBox) nomMatiere.getParent()).getChildren().add(nomError);
        ((HBox) descriptionMatiere.getParent()).getChildren().add(descriptionError);
        ((HBox) objectifMatiere.getParent()).getChildren().add(objectifError);
        ((HBox) moduleId.getParent()).getChildren().add(moduleIdError);
        ((HBox) enseignantId.getParent()).getChildren().add(enseignantIdError);
        ((HBox) prerequisMatiere.getParent()).getChildren().add(prerequisError);
    }

    private void setupValidationListeners() {
        // Validation du nom (ne doit pas être vide)
        nomMatiere.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nomMatiere.setStyle(INVALID_STYLE);
                nomError.setText("Le nom est obligatoire");
                nomError.setVisible(true);
            } else {
                nomMatiere.setStyle(VALID_STYLE);
                nomError.setVisible(false);
            }
        });

        // Validation de la description (ne doit pas être vide)
        descriptionMatiere.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                descriptionMatiere.setStyle(INVALID_STYLE);
                descriptionError.setText("La description est obligatoire");
                descriptionError.setVisible(true);
            } else {
                descriptionMatiere.setStyle(VALID_STYLE);
                descriptionError.setVisible(false);
            }
        });

        // Validation de l'objectif (ne doit pas être vide)
        objectifMatiere.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                objectifMatiere.setStyle(INVALID_STYLE);
                objectifError.setText("L'objectif est obligatoire");
                objectifError.setVisible(true);
            } else {
                objectifMatiere.setStyle(VALID_STYLE);
                objectifError.setVisible(false);
            }
        });

        // Validation du moduleId (doit être un nombre entier positif)
        moduleId.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    moduleId.setStyle(INVALID_STYLE);
                    moduleIdError.setText("L'ID du module est obligatoire");
                    moduleIdError.setVisible(true);
                } else {
                    int value = Integer.parseInt(newValue);
                    if (value <= 0) {
                        moduleId.setStyle(INVALID_STYLE);
                        moduleIdError.setText("L'ID doit être positif");
                        moduleIdError.setVisible(true);
                    } else {
                        moduleId.setStyle(VALID_STYLE);
                        moduleIdError.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                moduleId.setStyle(INVALID_STYLE);
                moduleIdError.setText("Veuillez entrer un nombre valide");
                moduleIdError.setVisible(true);
            }
        });

        // Validation de l'enseignantId (doit être un nombre entier positif)
        enseignantId.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    enseignantId.setStyle(INVALID_STYLE);
                    enseignantIdError.setText("L'ID de l'enseignant est obligatoire");
                    enseignantIdError.setVisible(true);
                } else {
                    int value = Integer.parseInt(newValue);
                    if (value <= 0) {
                        enseignantId.setStyle(INVALID_STYLE);
                        enseignantIdError.setText("L'ID doit être positif");
                        enseignantIdError.setVisible(true);
                    } else {
                        enseignantId.setStyle(VALID_STYLE);
                        enseignantIdError.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                enseignantId.setStyle(INVALID_STYLE);
                enseignantIdError.setText("Veuillez entrer un nombre valide");
                enseignantIdError.setVisible(true);
            }
        });

        // Validation du prérequis (doit être un nombre entier positif si non vide)
        prerequisMatiere.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                prerequisMatiere.setStyle(NORMAL_STYLE);
                prerequisError.setVisible(false);
            } else {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value <= 0) {
                        prerequisMatiere.setStyle(INVALID_STYLE);
                        prerequisError.setText("L'ID doit être positif");
                        prerequisError.setVisible(true);
                    } else {
                        prerequisMatiere.setStyle(VALID_STYLE);
                        prerequisError.setVisible(false);
                    }
                } catch (NumberFormatException e) {
                    prerequisMatiere.setStyle(INVALID_STYLE);
                    prerequisError.setText("Veuillez entrer un nombre valide");
                    prerequisError.setVisible(true);
                }
            }
        });
    }

    public void initData(Matiere matiere) {
        if (matiere != null) {
            this.matiereToEdit = matiere;
            this.isEditMode = true;
            btnSave.setText("Modifier");

            nomMatiere.setText(matiere.getNomMatiere());
            descriptionMatiere.setText(matiere.getDescriptionMatiere());
            objectifMatiere.setText(matiere.getObjectifMatiere());
            moduleId.setText(String.valueOf(matiere.getModuleId()));
            enseignantId.setText(String.valueOf(matiere.getEnseignantId()));

            if (matiere.getPrerequisMatiere() != null) {
                prerequisMatiere.setText(String.valueOf(matiere.getPrerequisMatiere()));
            } else {
                prerequisMatiere.setText("");
            }

            // Appliquer la validation initiale
            validateAllFields();
        }
    }

    private void validateAllFields() {
        // Déclencher la validation pour tous les champs
        String nomValue = nomMatiere.getText();
        nomMatiere.setText(nomValue + " ");
        nomMatiere.setText(nomValue);

        String descriptionValue = descriptionMatiere.getText();
        descriptionMatiere.setText(descriptionValue + " ");
        descriptionMatiere.setText(descriptionValue);

        String objectifValue = objectifMatiere.getText();
        objectifMatiere.setText(objectifValue + " ");
        objectifMatiere.setText(objectifValue);

        String moduleIdValue = moduleId.getText();
        moduleId.setText(moduleIdValue + " ");
        moduleId.setText(moduleIdValue);

        String enseignantIdValue = enseignantId.getText();
        enseignantId.setText(enseignantIdValue + " ");
        enseignantId.setText(enseignantIdValue);

        String prerequisValue = prerequisMatiere.getText();
        prerequisMatiere.setText(prerequisValue + " ");
        prerequisMatiere.setText(prerequisValue);
    }

    @FXML
    void saveMatiere(ActionEvent event) {
        // Vérifier si tous les champs sont valides avant de sauvegarder
        if (!validateForm()) {
            showError("Veuillez corriger les erreurs dans le formulaire avant de sauvegarder.");
            return;
        }

        try {
            String nom = nomMatiere.getText().trim();
            String description = descriptionMatiere.getText().trim();
            String objectif = objectifMatiere.getText().trim();
            int moduleIdValue = Integer.parseInt(moduleId.getText().trim());
            int enseignantIdValue = Integer.parseInt(enseignantId.getText().trim());

            Integer prerequisValue = null;
            if (!prerequisMatiere.getText().trim().isEmpty()) {
                prerequisValue = Integer.parseInt(prerequisMatiere.getText().trim());
            }

            if (isEditMode && matiereToEdit != null) {
                // Mettre à jour une matière existante
                Matiere updatedMatiere = new Matiere(
                        matiereToEdit.getId(),
                        moduleIdValue,
                        enseignantIdValue,
                        nom,
                        description,
                        prerequisValue,
                        objectif);
                matiereService.updateMatiere(updatedMatiere);
                showAlert("Matière modifiée avec succès !");
            } else {
                // Créer une nouvelle matière
                Matiere newMatiere = new Matiere(
                        moduleIdValue,
                        enseignantIdValue,
                        nom,
                        description,
                        prerequisValue,
                        objectif);
                matiereService.addMatiere(newMatiere);
                showAlert("Matière ajoutée avec succès !");
            }

            if (onSaveCallback != null)
                onSaveCallback.run();

            // Fermer la fenêtre
            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError(
                    "Erreur: Veuillez entrer des valeurs numériques valides pour les champs Module ID, Enseignant ID et Prérequis.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Vérifier le nom
        if (nomMatiere.getText().trim().isEmpty()) {
            nomMatiere.setStyle(INVALID_STYLE);
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            isValid = false;
        }

        // Vérifier la description
        if (descriptionMatiere.getText().trim().isEmpty()) {
            descriptionMatiere.setStyle(INVALID_STYLE);
            descriptionError.setText("La description est obligatoire");
            descriptionError.setVisible(true);
            isValid = false;
        }

        // Vérifier l'objectif
        if (objectifMatiere.getText().trim().isEmpty()) {
            objectifMatiere.setStyle(INVALID_STYLE);
            objectifError.setText("L'objectif est obligatoire");
            objectifError.setVisible(true);
            isValid = false;
        }

        // Vérifier le moduleId
        try {
            if (moduleId.getText().trim().isEmpty()) {
                moduleId.setStyle(INVALID_STYLE);
                moduleIdError.setText("L'ID du module est obligatoire");
                moduleIdError.setVisible(true);
                isValid = false;
            } else {
                int value = Integer.parseInt(moduleId.getText().trim());
                if (value <= 0) {
                    moduleId.setStyle(INVALID_STYLE);
                    moduleIdError.setText("L'ID doit être positif");
                    moduleIdError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            moduleId.setStyle(INVALID_STYLE);
            moduleIdError.setText("Veuillez entrer un nombre valide");
            moduleIdError.setVisible(true);
            isValid = false;
        }

        // Vérifier l'enseignantId
        try {
            if (enseignantId.getText().trim().isEmpty()) {
                enseignantId.setStyle(INVALID_STYLE);
                enseignantIdError.setText("L'ID de l'enseignant est obligatoire");
                enseignantIdError.setVisible(true);
                isValid = false;
            } else {
                int value = Integer.parseInt(enseignantId.getText().trim());
                if (value <= 0) {
                    enseignantId.setStyle(INVALID_STYLE);
                    enseignantIdError.setText("L'ID doit être positif");
                    enseignantIdError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            enseignantId.setStyle(INVALID_STYLE);
            enseignantIdError.setText("Veuillez entrer un nombre valide");
            enseignantIdError.setVisible(true);
            isValid = false;
        }

        // Vérifier le prérequis (optionnel)
        if (!prerequisMatiere.getText().trim().isEmpty()) {
            try {
                int value = Integer.parseInt(prerequisMatiere.getText().trim());
                if (value <= 0) {
                    prerequisMatiere.setStyle(INVALID_STYLE);
                    prerequisError.setText("L'ID doit être positif");
                    prerequisError.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                prerequisMatiere.setStyle(INVALID_STYLE);
                prerequisError.setText("Veuillez entrer un nombre valide");
                prerequisError.setVisible(true);
                isValid = false;
            }
        }

        return isValid;
    }

    @FXML
    void cancelMatiere(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
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
}