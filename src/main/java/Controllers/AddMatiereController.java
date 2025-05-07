package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import Entities.Matiere;
import Entities.Module;
import Entities.User;
import java.util.List;
import Services.ModuleService;
import Services.ModuleServiceImpl;
import Services.UserService;
import Services.UserServiceImplementation;
import utils.MyDatabase;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import Services.ImageSearchService;

public class AddMatiereController {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField nomMatiere;
    @FXML
    private TextArea descriptionMatiere;
    @FXML
    private TextField objectifMatiere;
    @FXML
    private ComboBox<String> moduleId;
    @FXML
    private ComboBox<String> enseignantId;

    private ModuleService moduleService = new ModuleServiceImpl();
    private UserService userService = new UserServiceImplementation(MyDatabase.getInstance().getConnection());
    @FXML
    private TextField prerequisMatiere;

    private Label nomError;
    private Label descriptionError;
    private Label objectifError;
    private Label moduleIdError;
    private Label enseignantIdError;
    private Label prerequisError;

    private final String VALID_STYLE = "-fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-effect: dropshadow(three-pass-box, rgba(76,175,80,0.3), 10, 0, 0, 0);";
    private final String INVALID_STYLE = "-fx-border-color: #f44336; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-effect: dropshadow(three-pass-box, rgba(244,67,54,0.3), 10, 0, 0, 0);";
    private final String NORMAL_STYLE = "-fx-border-color: #E0E0E0; -fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-background-color: #FAFAFA; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-focus-color: #2196F3; -fx-faint-focus-color: #2196F322;";

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
        // Style du conteneur principal
        if (nomMatiere.getParent() != null && nomMatiere.getParent().getParent() instanceof VBox) {
            VBox mainContainer = (VBox) nomMatiere.getParent().getParent();
            mainContainer.setStyle(
                    "-fx-background-color: white; -fx-padding: 20px; -fx-spacing: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            mainContainer.setPadding(new javafx.geometry.Insets(20));
        }
        setupErrorLabels();

        // Appliquer le style moderne aux champs
        String baseStyle = "-fx-font-size: 14px; -fx-padding: 8px; -fx-transition: all 0.3s ease-in-out; -fx-background-insets: 0; "
                + NORMAL_STYLE;
        String hoverStyle = baseStyle
                + "-fx-background-color: #F5F5F5; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 8, 0, 0, 0);";
        String focusStyle = baseStyle
                + "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.3), 10, 0, 0, 0);";

        // Appliquer les effets de survol et de focus aux champs
        for (javafx.scene.Node field : new javafx.scene.Node[] { nomMatiere, descriptionMatiere, objectifMatiere,
                moduleId, enseignantId, prerequisMatiere }) {
            field.setOnMouseEntered(e -> field.setStyle(hoverStyle));
            field.setOnMouseExited(e -> field.setStyle(baseStyle));
            field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                field.setStyle(isNowFocused ? focusStyle : baseStyle);
            });
        }
        nomMatiere.setStyle(baseStyle);
        descriptionMatiere.setStyle(baseStyle + "-fx-pref-height: 100px;");
        objectifMatiere.setStyle(baseStyle);
        moduleId.setStyle(baseStyle);
        enseignantId.setStyle(baseStyle);
        prerequisMatiere.setStyle(baseStyle);

        // Style des boutons
        String buttonStyle = "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px; -fx-cursor: hand;";
        String saveButtonStyle = buttonStyle
                + "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.3), 10, 0, 0, 0);";
        String cancelButtonStyle = buttonStyle
                + "-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(158,158,158,0.3), 10, 0, 0, 0);";

        btnSave.setStyle(saveButtonStyle);
        btnCancel.setStyle(cancelButtonStyle);

        // Effet de survol pour les boutons
        btnSave.setOnMouseEntered(e -> btnSave.setStyle(saveButtonStyle + "-fx-background-color: #1976D2;"));
        btnSave.setOnMouseExited(e -> btnSave.setStyle(saveButtonStyle));
        btnSave.setOnMousePressed(e -> btnSave.setStyle(saveButtonStyle + "-fx-background-color: #0D47A1;"));
        btnSave.setOnMouseReleased(e -> btnSave.setStyle(saveButtonStyle));

        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(cancelButtonStyle + "-fx-background-color: #757575;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(cancelButtonStyle));
        btnCancel.setOnMousePressed(e -> btnCancel.setStyle(cancelButtonStyle + "-fx-background-color: #424242;"));
        btnCancel.setOnMouseReleased(e -> btnCancel.setStyle(cancelButtonStyle));

        // Configurer les tooltips
        nomMatiere.setTooltip(nomTooltip);
        descriptionMatiere.setTooltip(descriptionTooltip);
        objectifMatiere.setTooltip(objectifTooltip);
        moduleId.setTooltip(moduleIdTooltip);
        enseignantId.setTooltip(enseignantIdTooltip);
        prerequisMatiere.setTooltip(prerequisTooltip);

        // Charger les modules
        List<Module> modules = moduleService.getAllModules();
        ObservableList<String> moduleItems = FXCollections.observableArrayList();
        for (Module module : modules) {
            moduleItems.add(module.getNom_module());
        }
        moduleId.setItems(moduleItems);

        // Charger les enseignants
        List<User> users = userService.getUsers();
        ObservableList<String> teacherItems = FXCollections.observableArrayList();
        for (User user : users) {
            if (user.getRole().equals("ROLE_TEACHER")) {
                teacherItems.add(user.getNom() + " " + user.getPrenom());
            }
        }
        enseignantId.setItems(teacherItems);

        setupValidationListeners();
    }

    private void setupErrorLabels() {
        String errorLabelStyle = "-fx-font-size: 12px; -fx-padding: 5px 0; -fx-text-fill: #f44336; -fx-font-style: italic;";

        nomError = new Label();
        nomError.setStyle(errorLabelStyle);
        nomError.setVisible(false);

        descriptionError = new Label();
        descriptionError.setStyle(errorLabelStyle);
        descriptionError.setVisible(false);

        objectifError = new Label();
        objectifError.setStyle(errorLabelStyle);
        objectifError.setVisible(false);

        moduleIdError = new Label();
        moduleIdError.setStyle(errorLabelStyle);
        moduleIdError.setVisible(false);

        enseignantIdError = new Label();
        enseignantIdError.setStyle(errorLabelStyle);
        enseignantIdError.setVisible(false);

        prerequisError = new Label();
        prerequisError.setStyle(errorLabelStyle);
        prerequisError.setVisible(false);

        // Style des tooltips
        String tooltipStyle = "-fx-font-size: 12px; -fx-padding: 8px; -fx-background-color: #424242; -fx-text-fill: white; -fx-background-radius: 4px;";
        nomTooltip.setStyle(tooltipStyle);
        descriptionTooltip.setStyle(tooltipStyle);
        objectifTooltip.setStyle(tooltipStyle);
        moduleIdTooltip.setStyle(tooltipStyle);
        enseignantIdTooltip.setStyle(tooltipStyle);
        prerequisTooltip.setStyle(tooltipStyle);

        // Ajouter les labels d'erreur avec espacement
        for (HBox parent : new HBox[] { (HBox) nomMatiere.getParent(),
                (HBox) descriptionMatiere.getParent(),
                (HBox) objectifMatiere.getParent(),
                (HBox) moduleId.getParent(),
                (HBox) enseignantId.getParent(),
                (HBox) prerequisMatiere.getParent() }) {
            parent.setSpacing(10);
            parent.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
        }

        ((HBox) nomMatiere.getParent()).getChildren().add(nomError);
        ((HBox) descriptionMatiere.getParent()).getChildren().add(descriptionError);
        ((HBox) objectifMatiere.getParent()).getChildren().add(objectifError);
        ((HBox) moduleId.getParent()).getChildren().add(moduleIdError);
        ((HBox) enseignantId.getParent()).getChildren().add(enseignantIdError);
        ((HBox) prerequisMatiere.getParent()).getChildren().add(prerequisError);

        // Ajouter des transitions pour les messages d'erreur
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200));
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        for (Label errorLabel : new Label[] { nomError, descriptionError, objectifError, moduleIdError,
                enseignantIdError, prerequisError }) {
            errorLabel.setOpacity(0);
            errorLabel.visibleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    fadeTransition.setNode(errorLabel);
                    fadeTransition.playFromStart();
                }
            });
        }
    }

    private void setupValidationListeners() {

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

        // Configuration supplémentaire pour la zone de texte de description
        descriptionMatiere.setWrapText(true);
        descriptionMatiere.setPrefRowCount(5);
        descriptionMatiere.setPrefWidth(300);
        descriptionMatiere.setStyle(
                "-fx-font-size: 13px; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-border-radius: 4px;");

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

        moduleId.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                moduleId.setStyle(INVALID_STYLE);
                moduleIdError.setText("Le module est obligatoire");
                moduleIdError.setVisible(true);
            } else {
                moduleId.setStyle(VALID_STYLE);
                moduleIdError.setVisible(false);
            }
        });

        enseignantId.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                enseignantId.setStyle(INVALID_STYLE);
                enseignantIdError.setText("L'enseignant est obligatoire");
                enseignantIdError.setVisible(true);
            } else {
                enseignantId.setStyle(VALID_STYLE);
                enseignantIdError.setVisible(false);
            }
        });

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
            // Sélectionner le module
            for (Module module : moduleService.getAllModules()) {
                if (module.getId() == matiere.getModuleId()) {
                    moduleId.setValue(module.getNom_module());
                    break;
                }
            }

            // Sélectionner l'enseignant
            for (User user : userService.getUsers()) {
                if (user.getRole().equals("ROLE_TEACHER") && user.getId() == matiere.getEnseignantId()) {
                    enseignantId.setValue(user.getNom() + " " + user.getPrenom());
                    break;
                }
            }

            if (matiere.getPrerequisMatiere() != null) {
                prerequisMatiere.setText(String.valueOf(matiere.getPrerequisMatiere()));
            } else {
                prerequisMatiere.setText("");
            }

            validateAllFields();
        }
    }

    public void setMatiere(Matiere matiere) {
        initData(matiere);
    }

    private void validateAllFields() {

        String nomValue = nomMatiere.getText();
        nomMatiere.setText(nomValue + " ");
        nomMatiere.setText(nomValue);

        String descriptionValue = descriptionMatiere.getText();
        descriptionMatiere.setText(descriptionValue + " ");
        descriptionMatiere.setText(descriptionValue);

        String objectifValue = objectifMatiere.getText();
        objectifMatiere.setText(objectifValue + " ");
        objectifMatiere.setText(objectifValue);

        String moduleIdValue = moduleId.getValue();
        if (moduleIdValue != null) {
            moduleId.setValue(moduleIdValue);
        }

        String enseignantIdValue = enseignantId.getValue();
        if (enseignantIdValue != null) {
            enseignantId.setValue(enseignantIdValue);
        }

        String prerequisValue = prerequisMatiere.getText();
        prerequisMatiere.setText(prerequisValue + " ");
        prerequisMatiere.setText(prerequisValue);
    }

    @FXML
    void saveMatiere(ActionEvent event) {

        if (!validateForm()) {
            showError("Veuillez corriger les erreurs dans le formulaire avant de sauvegarder.");
            return;
        }

        try {
            String nom = nomMatiere.getText().trim();
            String description = descriptionMatiere.getText().trim();
            String objectif = objectifMatiere.getText().trim();
            String selectedModuleName = moduleId.getValue();
            String selectedTeacherName = enseignantId.getValue();

            // Trouver l'ID du module sélectionné
            int moduleIdValue = -1;
            for (Module module : moduleService.getAllModules()) {
                if (module.getNom_module().equals(selectedModuleName)) {
                    moduleIdValue = module.getId();
                    break;
                }
            }

            // Trouver l'ID de l'enseignant sélectionné
            int enseignantIdValue = -1;
            for (User user : userService.getUsers()) {
                if (user.getRole().equals("ROLE_TEACHER") &&
                        (user.getNom() + " " + user.getPrenom()).equals(selectedTeacherName)) {
                    enseignantIdValue = user.getId();
                    break;
                }
            }

            if (moduleIdValue == -1 || enseignantIdValue == -1) {
                showError("Erreur: Module ou enseignant non trouvé");
                return;
            }

            Integer prerequisValue = null;
            if (!prerequisMatiere.getText().trim().isEmpty()) {
                prerequisValue = Integer.parseInt(prerequisMatiere.getText().trim());
            }

            if (isEditMode && matiereToEdit != null) {

                String imageUrl = ImageSearchService.searchImageForMatiere(nom);
                Matiere updatedMatiere = new Matiere(
                        matiereToEdit.getId(),
                        moduleIdValue,
                        enseignantIdValue,
                        nom,
                        description,
                        prerequisValue,
                        objectif,
                        imageUrl);
                matiereService.updateMatiere(updatedMatiere);
                showAlert("Matière modifiée avec succès !");
            } else {

                String imageUrl = ImageSearchService.searchImageForMatiere(nom);
                Matiere newMatiere = new Matiere(
                        moduleIdValue,
                        enseignantIdValue,
                        nom,
                        description,
                        prerequisValue,
                        objectif,
                        imageUrl);
                matiereService.addMatiere(newMatiere);
                showAlert("Matière ajoutée avec succès !");
            }

            if (onSaveCallback != null)
                onSaveCallback.run();

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

        if (nomMatiere.getText().trim().isEmpty()) {
            nomMatiere.setStyle(INVALID_STYLE);
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            isValid = false;
        }

        if (descriptionMatiere.getText().trim().isEmpty()) {
            descriptionMatiere.setStyle(INVALID_STYLE);
            descriptionError.setText("La description est obligatoire");
            descriptionError.setVisible(true);
            isValid = false;
        }

        if (objectifMatiere.getText().trim().isEmpty()) {
            objectifMatiere.setStyle(INVALID_STYLE);
            objectifError.setText("L'objectif est obligatoire");
            objectifError.setVisible(true);
            isValid = false;
        }

        if (moduleId.getValue() == null) {
            moduleId.setStyle(INVALID_STYLE);
            moduleIdError.setText("Le module est obligatoire");
            moduleIdError.setVisible(true);
            isValid = false;
        }

        if (enseignantId.getValue() == null) {
            enseignantId.setStyle(INVALID_STYLE);
            enseignantIdError.setText("L'enseignant est obligatoire");
            enseignantIdError.setVisible(true);
            isValid = false;
        }

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