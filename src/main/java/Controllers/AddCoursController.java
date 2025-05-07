package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import Entities.Cours;
import Entities.Matiere;
import Services.CoursService;
import Services.CoursServiceImpl;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import Services.GeminiService;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.TextArea;
import java.util.List;

public class AddCoursController {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField nomCours;
    @FXML
    private TextField ordre;
    @FXML
    private TextField niveau;
    @FXML
    private Button btnImportPdf;
    @FXML
    private Label pdfFileName;
    @FXML
    private ComboBox<String> matiereId;
    @FXML
    private ComboBox<String> prerequisCoursId;
    @FXML
    private TextArea descriptionCours;
    @FXML
    private Button btnGenerateDescription;

    private Label nomError;
    private Label descriptionError;
    private Label ordreError;
    private Label niveauError;
    private Label matiereIdError;
    private Label prerequisError;

    private final String VALID_STYLE = "-fx-background-color: #f0fff0; -fx-border-color: #4caf50; -fx-border-width: 1.5px; -fx-effect: dropshadow(gaussian, #4caf5033, 4, 0, 0, 0);";
    private final String INVALID_STYLE = "-fx-background-color: #fff0f0; -fx-border-color: #f44336; -fx-border-width: 1.5px; -fx-effect: dropshadow(gaussian, #f4433633, 4, 0, 0, 0);";
    private final String NORMAL_STYLE = "-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, #00000011, 4, 0, 0, 0);";

    private boolean isEditMode = false;
    private Cours coursToEdit;
    private Runnable onSaveCallback;

    private CoursService coursService = new CoursServiceImpl();
    private File selectedPdfFile = null;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private MatiereService matiereService = new MatiereServiceImpl();

    @FXML
    public void initialize() {
        setupErrorLabels();
        setupValidationListeners();
        setupFieldStyles();
        loadMatieresAndCours();

        btnImportPdf.setOnAction(e -> importPdfFile());

        if (btnGenerateDescription != null) {
            btnGenerateDescription.setOnAction(e -> generateDescription());
        }

        descriptionCours.setWrapText(true);
    }

    private void loadMatieresAndCours() {
        // Charger les matières
        List<Matiere> matieres = matiereService.getAllMatieres();
        matiereId.getItems().clear();
        for (Matiere matiere : matieres) {
            matiereId.getItems().add(matiere.getNomMatiere());
        }

        // Charger les cours pour les prérequis
        List<Cours> cours = coursService.getAllCours();
        prerequisCoursId.getItems().clear();
        prerequisCoursId.getItems().add("Aucun prérequis");
        for (Cours c : cours) {
            prerequisCoursId.getItems().add(c.getNomCours());
        }
    }

    private Integer getSelectedMatiereId() {
        String selectedNom = matiereId.getValue();
        if (selectedNom != null && !selectedNom.isEmpty()) {
            for (Matiere matiere : matiereService.getAllMatieres()) {
                if (matiere.getNomMatiere().equals(selectedNom)) {
                    return matiere.getId();
                }
            }
        }
        return null;
    }

    private Integer getSelectedPrerequisId() {
        String selectedNom = prerequisCoursId.getValue();
        if (selectedNom != null && !selectedNom.isEmpty() && !selectedNom.equals("Aucun prérequis")) {
            for (Cours cours : coursService.getAllCours()) {
                if (cours.getNomCours().equals(selectedNom)) {
                    return cours.getId();
                }
            }
        }
        return null;
    }

    private void setupFieldStyles() {
        // Style pour les tooltips
        String tooltipStyle = "-fx-background-color: #424242; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 12; -fx-background-radius: 4;";

        // Style de base pour tous les champs texte
        TextField[] textFields = { nomCours, ordre, niveau };
        String[] textFieldTooltips = {
                "Entrez le nom du cours",
                "Numéro d'ordre du cours dans le programme",
                "Niveau de difficulté du cours"
        };

        for (int i = 0; i < textFields.length; i++) {
            TextField field = textFields[i];
            field.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");

            // Configuration du tooltip
            Tooltip tooltip = new Tooltip(textFieldTooltips[i]);
            tooltip.setStyle(tooltipStyle);
            tooltip.setShowDelay(javafx.util.Duration.millis(200));
            tooltip.setHideDelay(javafx.util.Duration.millis(200));
            field.setTooltip(tooltip);

            // Effet de survol
            field.setOnMouseEntered(e -> {
                field.setStyle(field.getStyle() + "; -fx-background-color: #f5f5f5;");
                tooltip.setStyle(tooltipStyle + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
            });
            field.setOnMouseExited(e -> {
                field.setStyle(field.getStyle().replace("; -fx-background-color: #f5f5f5;", ""));
                tooltip.setStyle(tooltipStyle);
            });

            // Effet de focus
            field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    field.setStyle(field.getStyle() + "; -fx-border-color: #2196f3; -fx-border-width: 2;");
                } else {
                    field.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });
        }

        // Style pour les ComboBox
        ComboBox<?>[] comboBoxes = { matiereId, prerequisCoursId };
        String[] comboBoxTooltips = {
                "Sélectionnez la matière associée",
                "Sélectionnez le cours prérequis (optionnel)"
        };

        for (int i = 0; i < comboBoxes.length; i++) {
            ComboBox<?> comboBox = comboBoxes[i];
            comboBox.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");

            // Configuration du tooltip
            Tooltip tooltip = new Tooltip(comboBoxTooltips[i]);
            tooltip.setStyle(tooltipStyle);
            tooltip.setShowDelay(javafx.util.Duration.millis(200));
            tooltip.setHideDelay(javafx.util.Duration.millis(200));
            comboBox.setTooltip(tooltip);

            // Effet de survol
            comboBox.setOnMouseEntered(e -> {
                comboBox.setStyle(comboBox.getStyle() + "; -fx-background-color: #f5f5f5;");
                tooltip.setStyle(tooltipStyle + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
            });
            comboBox.setOnMouseExited(e -> {
                comboBox.setStyle(comboBox.getStyle().replace("; -fx-background-color: #f5f5f5;", ""));
                tooltip.setStyle(tooltipStyle);
            });

            // Effet de focus
            comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    comboBox.setStyle(comboBox.getStyle() + "; -fx-border-color: #2196f3; -fx-border-width: 2;");
                } else {
                    comboBox.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });
        }

        // Style spécial pour le TextArea
        descriptionCours.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        descriptionCours.setOnMouseEntered(
                e -> descriptionCours.setStyle(descriptionCours.getStyle() + "; -fx-background-color: #f5f5f5;"));
        descriptionCours.setOnMouseExited(e -> descriptionCours
                .setStyle(descriptionCours.getStyle().replace("; -fx-background-color: #f5f5f5;", "")));
        descriptionCours.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                descriptionCours
                        .setStyle(descriptionCours.getStyle() + "; -fx-border-color: #2196f3; -fx-border-width: 2;");
            } else {
                descriptionCours.setStyle(NORMAL_STYLE + "; -fx-background-radius: 8; -fx-border-radius: 8;");
            }
        });

        // Style pour les boutons
        btnImportPdf.setStyle(
                "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");
        btnImportPdf.setOnMouseEntered(
                e -> btnImportPdf.setStyle(btnImportPdf.getStyle() + "; -fx-background-color: #bbdefb;"));
        btnImportPdf.setOnMouseExited(
                e -> btnImportPdf.setStyle(btnImportPdf.getStyle().replace("; -fx-background-color: #bbdefb", "")));
    }

    private void setupErrorLabels() {
        String errorLabelStyle = "-fx-text-fill: #f44336; -fx-font-size: 12px; -fx-padding: 4 0 0 0; -fx-font-style: italic;";

        // Création des labels d'erreur avec style uniforme
        Label[] errorLabels = {
                nomError = new Label(),
                descriptionError = new Label(),
                ordreError = new Label(),
                niveauError = new Label(),
                matiereIdError = new Label(),
                prerequisError = new Label()
        };

        // Application du style et configuration des transitions pour tous les labels
        for (Label label : errorLabels) {
            label.setStyle(errorLabelStyle);
            label.setVisible(false);
            label.setManaged(false);

            // Ajout d'une transition de fondu
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), label);
            label.visibleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    label.setManaged(true);
                    fadeTransition.setFromValue(0.0);
                    fadeTransition.setToValue(1.0);
                    fadeTransition.play();
                } else {
                    fadeTransition.setFromValue(1.0);
                    fadeTransition.setToValue(0.0);
                    fadeTransition.setOnFinished(e -> label.setManaged(false));
                    fadeTransition.play();
                }
            });
        }

        // Ajout des labels aux conteneurs parents avec espacement
        ((HBox) nomCours.getParent()).getChildren().add(nomError);
        ((VBox) descriptionCours.getParent()).getChildren().add(descriptionError);
        ((HBox) ordre.getParent()).getChildren().add(ordreError);
        ((HBox) niveau.getParent()).getChildren().add(niveauError);
        ((HBox) matiereId.getParent()).getChildren().add(matiereIdError);
        ((HBox) prerequisCoursId.getParent()).getChildren().add(prerequisError);

        // Ajout d'espacement pour les conteneurs
        HBox[] containers = {
                (HBox) nomCours.getParent(),
                (HBox) ordre.getParent(),
                (HBox) niveau.getParent(),
                (HBox) matiereId.getParent(),
                (HBox) prerequisCoursId.getParent()
        };

        for (HBox container : containers) {
            container.setSpacing(10);
            container.setPadding(new javafx.geometry.Insets(5));
        }
    }

    private void setupValidationListeners() {
        nomCours.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                nomCours.setStyle(INVALID_STYLE);
                nomError.setText("Le nom est obligatoire");
                nomError.setVisible(true);
            } else {
                nomCours.setStyle(VALID_STYLE);
                nomError.setVisible(false);
            }
        });

        ordre.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal.trim().isEmpty()) {
                    ordre.setStyle(INVALID_STYLE);
                    ordreError.setText("L'ordre est obligatoire");
                    ordreError.setVisible(true);
                } else {
                    int value = Integer.parseInt(newVal);
                    if (value <= 0) {
                        ordre.setStyle(INVALID_STYLE);
                        ordreError.setText("L'ordre doit être positif");
                        ordreError.setVisible(true);
                    } else {
                        ordre.setStyle(VALID_STYLE);
                        ordreError.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                ordre.setStyle(INVALID_STYLE);
                ordreError.setText("Veuillez entrer un nombre valide");
                ordreError.setVisible(true);
            }
        });

        niveau.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                niveau.setStyle(INVALID_STYLE);
                niveauError.setText("Le niveau est obligatoire");
                niveauError.setVisible(true);
            } else {
                niveau.setStyle(VALID_STYLE);
                niveauError.setVisible(false);
            }
        });

        matiereId.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                matiereId.setStyle(INVALID_STYLE);
                matiereIdError.setText("La matière est obligatoire");
                matiereIdError.setVisible(true);
            } else {
                matiereId.setStyle(VALID_STYLE);
                matiereIdError.setVisible(false);
            }
        });

        prerequisCoursId.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                prerequisCoursId.setStyle(NORMAL_STYLE);
                prerequisError.setVisible(false);
            } else if (!newVal.equals("Aucun prérequis")) {
                prerequisCoursId.setStyle(VALID_STYLE);
                prerequisError.setVisible(false);
            }
        });
    }

    private void importPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(btnImportPdf.getScene().getWindow());
        if (file != null) {
            selectedPdfFile = file;
            pdfFileName.setText(file.getName());
        }
    }

    public void initData(Cours cours) {
        if (cours != null) {
            this.coursToEdit = cours;
            this.isEditMode = true;
            btnSave.setText("Modifier");

            nomCours.setText(cours.getNomCours());
            descriptionCours.setText(cours.getDescriptionCours());
            ordre.setText(String.valueOf(cours.getOrdre()));
            niveau.setText(cours.getNiveau());
            pdfFileName.setText(cours.getPdfCours() != null ? cours.getPdfCours() : "");
            // Charger les listes avant de sélectionner les valeurs
            loadMatieresAndCours();

            // Sélectionner la matière
            Matiere matiere = matiereService.getMatiereById(cours.getMatiereId());
            if (matiere != null) {
                matiereId.setValue(matiere.getNomMatiere());
            }

            // Sélectionner le prérequis
            if (cours.getPrerequisCoursId() != null) {
                Cours prerequis = coursService.getCoursById(cours.getPrerequisCoursId());
                if (prerequis != null) {
                    prerequisCoursId.setValue(prerequis.getNomCours());
                }
            } else {
                prerequisCoursId.setValue("Aucun prérequis");
            }
        }
    }

    @FXML
    void saveCours(ActionEvent event) {
        if (!validateForm()) {
            showError("Veuillez corriger les erreurs dans le formulaire avant de sauvegarder.");
            return;
        }

        try {
            String nom = nomCours.getText().trim();
            String description = descriptionCours.getText().trim();
            int ordreValue = Integer.parseInt(ordre.getText().trim());
            String status = "non lu";
            String niveauValue = niveau.getText().trim();
            Integer matiereIdValue = getSelectedMatiereId();
            Integer prerequisValue = getSelectedPrerequisId();

            String pdfFileNameValue = pdfFileName.getText();

            if (selectedPdfFile != null) {
                String destDir = "pdfs";
                Files.createDirectories(Path.of(destDir));
                Path destPath = Path.of(destDir, selectedPdfFile.getName());
                Files.copy(selectedPdfFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                pdfFileNameValue = selectedPdfFile.getName();
            }

            if (isEditMode && coursToEdit != null) {
                Cours updatedCours = new Cours(
                        coursToEdit.getId(),
                        prerequisValue,
                        matiereIdValue,
                        nom,
                        description,
                        ordreValue,
                        status,
                        niveauValue,
                        pdfFileNameValue,
                        null,
                        coursToEdit.getUpdatedAt());
                coursService.updateCours(updatedCours);
                showAlert("Cours modifié avec succès !");
            } else {
                Cours newCours = new Cours(
                        prerequisValue,
                        matiereIdValue,
                        nom,
                        description,
                        ordreValue,
                        status,
                        niveauValue,
                        pdfFileNameValue,
                        null);
                coursService.addCours(newCours);
                showAlert("Cours ajouté avec succès !");
            }

            if (onSaveCallback != null)
                onSaveCallback.run();

            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (nomCours.getText().trim().isEmpty()) {
            nomCours.setStyle(INVALID_STYLE);
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            isValid = false;
        }

        if (descriptionCours.getText().trim().isEmpty()) {
            descriptionCours.setStyle(INVALID_STYLE);
            descriptionError.setText("La description est obligatoire");
            descriptionError.setVisible(true);
            isValid = false;
        } else {
            descriptionCours.setStyle(VALID_STYLE);
            descriptionError.setVisible(false);
        }

        try {
            if (ordre.getText().trim().isEmpty()) {
                ordre.setStyle(INVALID_STYLE);
                ordreError.setText("L'ordre est obligatoire");
                ordreError.setVisible(true);
                isValid = false;
            } else {
                int value = Integer.parseInt(ordre.getText().trim());
                if (value <= 0) {
                    ordre.setStyle(INVALID_STYLE);
                    ordreError.setText("L'ordre doit être positif");
                    ordreError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            ordre.setStyle(INVALID_STYLE);
            ordreError.setText("Veuillez entrer un nombre valide");
            ordreError.setVisible(true);
            isValid = false;
        }
        if (niveau.getText().trim().isEmpty()) {
            niveau.setStyle(INVALID_STYLE);
            niveauError.setText("Le niveau est obligatoire");
            niveauError.setVisible(true);
            isValid = false;
        }
        if (matiereId.getValue() == null) {
            matiereId.setStyle(INVALID_STYLE);
            matiereIdError.setText("La matière est obligatoire");
            matiereIdError.setVisible(true);
            isValid = false;
        } else {
            matiereId.setStyle(VALID_STYLE);
            matiereIdError.setVisible(false);
        }

        // Le cours prérequis est optionnel, donc on vérifie uniquement s'il est
        // sélectionné
        if (prerequisCoursId.getValue() != null && !prerequisCoursId.getValue().equals("Aucun prérequis")) {
            prerequisCoursId.setStyle(VALID_STYLE);
            prerequisError.setVisible(false);
        }
        return isValid;
    }

    @FXML
    void cancelCours(ActionEvent event) {
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Génère une description pour le cours en utilisant l'API Gemini
     * basée sur le nom du cours et son niveau
     */
    private void generateDescription() {
        String nom = nomCours.getText().trim();
        String niveauValue = niveau.getText().trim();

        if (nom.isEmpty()) {
            nomCours.setStyle(INVALID_STYLE);
            nomError.setText("Le nom est obligatoire pour générer une description");
            nomError.setVisible(true);
            return;
        }

        // Afficher un indicateur de chargement
        descriptionCours.setDisable(true);
        btnGenerateDescription.setDisable(true);
        btnGenerateDescription.setText("Génération en cours...");

        // Utiliser un thread séparé pour ne pas bloquer l'interface utilisateur
        new Thread(() -> {
            try {
                GeminiService geminiService = new GeminiService();
                String description = geminiService.generateCourseDescription(nom, niveauValue);

                // Si la génération échoue, utiliser la méthode hors ligne
                if (description.startsWith("Erreur")) {
                    System.out.println("INFO: Erreur détectée, passage au mode hors ligne: " + description);
                    // Afficher l'erreur dans une alerte
                    final String errorMessage = description; // Rendre la variable finale pour l'utiliser dans la lambda
                    javafx.application.Platform.runLater(() -> {
                        showError("Problème avec l'API Gemini: " + errorMessage
                                + "\n\nUtilisation du mode hors ligne à la place.");
                    });
                    description = geminiService.generateDescriptionOffline(nom, niveauValue);
                }

                // Mettre à jour l'interface utilisateur dans le thread JavaFX
                String finalDescription = description;
                javafx.application.Platform.runLater(() -> {
                    descriptionCours.setText(finalDescription);
                    descriptionCours.setDisable(false);
                    btnGenerateDescription.setDisable(false);
                    btnGenerateDescription.setText("Générer Description");
                    descriptionCours.setStyle(VALID_STYLE);
                });
            } catch (Exception ex) {
                // Gérer les erreurs et mettre à jour l'interface utilisateur
                System.err.println("EXCEPTION dans generateDescription: " + ex.getMessage());
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Essayer de générer une description hors ligne en cas d'exception
                        GeminiService geminiService = new GeminiService();
                        String offlineDescription = geminiService.generateDescriptionOffline(nom, niveauValue);
                        descriptionCours.setText(offlineDescription);
                        showError("Erreur lors de la génération avec l'API: " + ex.getMessage()
                                + "\n\nUtilisation du mode hors ligne à la place.");
                    } catch (Exception e) {
                        descriptionCours.setText(
                                "Impossible de générer une description. Veuillez réessayer ou saisir manuellement.");
                        showError("Erreur critique: " + ex.getMessage());
                    }
                    descriptionCours.setDisable(false);
                    btnGenerateDescription.setDisable(false);
                    btnGenerateDescription.setText("Générer Description");
                });
            }
        }).start();
    }
}
