package Controllers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import Entities.Annonce;
import Services.AnnonceService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    @FXML private Button exportButton;
    @FXML private TextField searchField;

    private final AnnonceService annonceService = new AnnonceService();
    private Annonce currentAnnonce = null;
    private List<Annonce> allAnnonces;

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
        
        // Add search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAnnonces(newVal));
        
        loadAnnonces();
    }

    private void filterAnnonces(String searchText) {
        if (allAnnonces == null) return;
        
        List<Annonce> filteredAnnonces = allAnnonces.stream()
            .filter(annonce -> annonce.getTitre().toLowerCase().contains(searchText.toLowerCase()))
            .collect(Collectors.toList());
        
        displayAnnonces(filteredAnnonces);
    }

    private void displayAnnonces(List<Annonce> annonces) {
        cardsContainer.getChildren().clear();
        for (Annonce annonce : annonces) {
            cardsContainer.getChildren().add(createAnnonceCard(annonce));
        }
    }

    private void loadAnnonces() {
        try {
            allAnnonces = annonceService.readAll();
            displayAnnonces(allAnnonces);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des annonces: " + e.getMessage());
        }
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
                Annonce annonce = new Annonce(
                    null,
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
        formPopup.setVisible(true);
        
        if (annonce != null) {
            // Update form title and button text for edit mode
            saveButton.setText("Mettre à jour");
            titreField.setText(annonce.getTitre());
            descriptionArea.setText(annonce.getDescription());
            categorieField.setValue(annonce.getCategorie());
            
            // Validate fields immediately
            validateField(titreField, validateTitre(titreField.getText()));
            validateField(descriptionArea, validateDescription(descriptionArea.getText()));
            validateField(categorieField, validateCategorie(categorieField.getValue()));
        } else {
            // Reset form for add mode
            saveButton.setText("Ajouter");
            clearFields();
        }
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

    @FXML
    private void handleExportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            
            // Set default file name with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName("annonces_" + timestamp + ".pdf");
            
            java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            
            if (file != null) {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Add title
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Paragraph title = new Paragraph("Liste des Annonces", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);
                
                // Add timestamp
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                Paragraph timestamp_p = new Paragraph("Généré le: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), 
                    normalFont);
                timestamp_p.setSpacingAfter(20);
                document.add(timestamp_p);
                
                // Add announcements
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                for (Annonce annonce : annonceService.readAll()) {
                    // Add title
                    Paragraph annonceTitle = new Paragraph(annonce.getTitre(), headerFont);
                    annonceTitle.setSpacingBefore(15);
                    document.add(annonceTitle);
                    
                    // Add category
                    Paragraph category = new Paragraph("Catégorie: " + annonce.getCategorie(), normalFont);
                    category.setIndentationLeft(20);
                    document.add(category);
                    
                    // Add description
                    Paragraph description = new Paragraph(annonce.getDescription(), normalFont);
                    description.setIndentationLeft(20);
                    description.setSpacingAfter(10);
                    document.add(description);
                    
                    // Add separator
                    document.add(new Paragraph("----------------------------------------"));
                }
                
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Le PDF a été généré avec succès!\nEmplacement: " + file.getAbsolutePath());
            }
        } catch (DocumentException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la génération du PDF: " + e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la récupération des données: " + e.getMessage());
        }
    }

    @FXML
    private void showStatistics() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("statistics-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Statistiques des Annonces");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de l'affichage des statistiques: " + e.getMessage());
        }
    }
}