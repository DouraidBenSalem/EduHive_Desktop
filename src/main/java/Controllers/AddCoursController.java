package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import Entities.Cours;
import Services.CoursService;
import Services.CoursServiceImpl;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.TextArea;

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
    private TextField statusCours;
    @FXML
    private TextField niveau;
    @FXML
    private Button btnImportPdf;
    @FXML
    private Label pdfFileName;
    @FXML
    private TextField matiereId;
    @FXML
    private TextField prerequisCoursId;
    @FXML
    private TextArea descriptionCours;

    private Label nomError;
    private Label descriptionError;
    private Label ordreError;
    private Label statusError;
    private Label niveauError;
    private Label matiereIdError;
    private Label prerequisError;

    private final String VALID_STYLE = "-fx-border-color: green; -fx-border-width: 2px;";
    private final String INVALID_STYLE = "-fx-border-color: red; -fx-border-width: 2px;";
    private final String NORMAL_STYLE = "-fx-border-color: lightgray; -fx-border-width: 1px;";

    private boolean isEditMode = false;
    private Cours coursToEdit;
    private Runnable onSaveCallback;

    private CoursService coursService = new CoursServiceImpl();
    private File selectedPdfFile = null;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        setupErrorLabels();
        setupValidationListeners();

        btnImportPdf.setOnAction(e -> importPdfFile());

        // Configuration du TextArea pour la description
        descriptionCours.setWrapText(true);
    }

    private void setupErrorLabels() {
        nomError = new Label();
        nomError.setTextFill(Color.RED);
        nomError.setVisible(false);

        // descriptionError reste pour afficher une erreur sous le WebView
        descriptionError = new Label();
        descriptionError.setTextFill(Color.RED);
        descriptionError.setVisible(false);

        ordreError = new Label();
        ordreError.setTextFill(Color.RED);
        ordreError.setVisible(false);

        statusError = new Label();
        statusError.setTextFill(Color.RED);
        statusError.setVisible(false);

        niveauError = new Label();
        niveauError.setTextFill(Color.RED);
        niveauError.setVisible(false);

        matiereIdError = new Label();
        matiereIdError.setTextFill(Color.RED);
        matiereIdError.setVisible(false);

        prerequisError = new Label();
        prerequisError.setTextFill(Color.RED);
        prerequisError.setVisible(false);

        ((HBox) nomCours.getParent()).getChildren().add(nomError);

        ((VBox) descriptionCours.getParent()).getChildren().add(descriptionError);
        ((HBox) ordre.getParent()).getChildren().add(ordreError);
        ((HBox) statusCours.getParent()).getChildren().add(statusError);
        ((HBox) niveau.getParent()).getChildren().add(niveauError);
        ((HBox) matiereId.getParent()).getChildren().add(matiereIdError);
        ((HBox) prerequisCoursId.getParent()).getChildren().add(prerequisError);
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

        statusCours.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                statusCours.setStyle(INVALID_STYLE);
                statusError.setText("Le statut est obligatoire");
                statusError.setVisible(true);
            } else {
                statusCours.setStyle(VALID_STYLE);
                statusError.setVisible(false);
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

        matiereId.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal.trim().isEmpty()) {
                    matiereId.setStyle(INVALID_STYLE);
                    matiereIdError.setText("L'ID matière est obligatoire");
                    matiereIdError.setVisible(true);
                } else {
                    int value = Integer.parseInt(newVal);
                    if (value <= 0) {
                        matiereId.setStyle(INVALID_STYLE);
                        matiereIdError.setText("L'ID doit être positif");
                        matiereIdError.setVisible(true);
                    } else {
                        matiereId.setStyle(VALID_STYLE);
                        matiereIdError.setVisible(false);
                    }
                }
            } catch (NumberFormatException e) {
                matiereId.setStyle(INVALID_STYLE);
                matiereIdError.setText("Veuillez entrer un nombre valide");
                matiereIdError.setVisible(true);
            }
        });

        prerequisCoursId.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                prerequisCoursId.setStyle(NORMAL_STYLE);
                prerequisError.setVisible(false);
            } else {
                try {
                    int value = Integer.parseInt(newVal);
                    if (value <= 0) {
                        prerequisCoursId.setStyle(INVALID_STYLE);
                        prerequisError.setText("L'ID doit être positif");
                        prerequisError.setVisible(true);
                    } else {
                        prerequisCoursId.setStyle(VALID_STYLE);
                        prerequisError.setVisible(false);
                    }
                } catch (NumberFormatException e) {
                    prerequisCoursId.setStyle(INVALID_STYLE);
                    prerequisError.setText("Veuillez entrer un nombre valide");
                    prerequisError.setVisible(true);
                }
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
            statusCours.setText(cours.getStatusCours());
            niveau.setText(cours.getNiveau());
            pdfFileName.setText(cours.getPdfCours() != null ? cours.getPdfCours() : "");
            matiereId.setText(String.valueOf(cours.getMatiereId()));
            if (cours.getPrerequisCoursId() != null) {
                prerequisCoursId.setText(String.valueOf(cours.getPrerequisCoursId()));
            } else {
                prerequisCoursId.setText("");
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
            String status = statusCours.getText().trim();
            String niveauValue = niveau.getText().trim();
            int matiereIdValue = Integer.parseInt(matiereId.getText().trim());

            Integer prerequisValue = null;
            if (!prerequisCoursId.getText().trim().isEmpty()) {
                prerequisValue = Integer.parseInt(prerequisCoursId.getText().trim());
            }

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

        } catch (NumberFormatException e) {
            showError("Erreur: Veuillez entrer des valeurs numériques valides pour les champs numériques.");
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
        if (statusCours.getText().trim().isEmpty()) {
            statusCours.setStyle(INVALID_STYLE);
            statusError.setText("Le statut est obligatoire");
            statusError.setVisible(true);
            isValid = false;
        }
        if (niveau.getText().trim().isEmpty()) {
            niveau.setStyle(INVALID_STYLE);
            niveauError.setText("Le niveau est obligatoire");
            niveauError.setVisible(true);
            isValid = false;
        }
        try {
            if (matiereId.getText().trim().isEmpty()) {
                matiereId.setStyle(INVALID_STYLE);
                matiereIdError.setText("L'ID matière est obligatoire");
                matiereIdError.setVisible(true);
                isValid = false;
            } else {
                int value = Integer.parseInt(matiereId.getText().trim());
                if (value <= 0) {
                    matiereId.setStyle(INVALID_STYLE);
                    matiereIdError.setText("L'ID doit être positif");
                    matiereIdError.setVisible(true);
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            matiereId.setStyle(INVALID_STYLE);
            matiereIdError.setText("Veuillez entrer un nombre valide");
            matiereIdError.setVisible(true);
            isValid = false;
        }
        if (!prerequisCoursId.getText().trim().isEmpty()) {
            try {
                int value = Integer.parseInt(prerequisCoursId.getText().trim());
                if (value <= 0) {
                    prerequisCoursId.setStyle(INVALID_STYLE);
                    prerequisError.setText("L'ID doit être positif");
                    prerequisError.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                prerequisCoursId.setStyle(INVALID_STYLE);
                prerequisError.setText("Veuillez entrer un nombre valide");
                prerequisError.setVisible(true);
                isValid = false;
            }
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
