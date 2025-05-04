package Controllers;

import Entities.Cours;
import Services.CoursService;
import Services.CoursServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;
import javafx.concurrent.Task;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import java.util.Set;

public class UserCoursController implements Initializable {

    @FXML
    private FlowPane coursGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnRetourMatiere;

    private CoursService coursService;
    private ObservableList<Cours> coursList;
    private FilteredList<Cours> filteredCours;
    private Integer matiereId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coursService = new CoursServiceImpl();
        setupListeners();

        // Configuration du FlowPane
        coursGrid.setHgap(20);
        coursGrid.setVgap(20);
        coursGrid.setPadding(new Insets(20));

        // Attendre que la scène soit chargée pour appliquer les styles
        Platform.runLater(() -> {
            Scene scene = coursGrid.getScene();
            if (scene != null) {
                scene.getStylesheets().addAll(
                        getClass().getResource("/style_css/user_matiere_style.css").toExternalForm(),
                        getClass().getResource("/style_css/cours_card_style.css").toExternalForm());
            }
        });
    }

    public void setMatiereId(Integer id) {
        this.matiereId = id;
        loadCours();
    }

    private void loadCours() {
        if (matiereId != null) {
            List<Cours> cours = coursService.getCoursByMatiereId(matiereId);
            coursList = FXCollections.observableArrayList(cours);
            filteredCours = new FilteredList<>(coursList, _ -> true);
            updateCoursGrid();
        }
    }

    private void setupListeners() {
        searchField.textProperty().addListener((_, _, newValue) -> {
            String lower = newValue.toLowerCase();
            filteredCours.setPredicate(cours -> {
                if (lower.isEmpty())
                    return true;
                return cours.getNomCours().toLowerCase().contains(lower) ||
                        cours.getDescriptionCours().toLowerCase().contains(lower);
            });
            updateCoursGrid();
        });
    }

    private void updateCoursGrid() {
        coursGrid.getChildren().clear();

        for (Cours cours : filteredCours) {
            VBox card = createCoursCard(cours);
            coursGrid.getChildren().add(card);
        }
    }

    private VBox createCoursCard(Cours cours) {
        VBox card = new VBox(15);
        card.getStyleClass().add("cours-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(320);

        // Titre du cours avec style amélioré
        Label titleLabel = new Label(cours.getNomCours());
        titleLabel.getStyleClass().add("cours-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Conteneur d'informations avec espacement amélioré
        HBox infoContainer = new HBox(20);
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.setPadding(new Insets(10, 0, 10, 0));

        Label niveauLabel = new Label("Niveau: " + cours.getNiveau());
        niveauLabel.getStyleClass().add("cours-info");

        // Ajouter la classe de style en fonction du niveau
        String niveau = cours.getNiveau().toLowerCase();
        if (niveau.contains("débutant") || niveau.contains("debutant")) {
            niveauLabel.getStyleClass().add("niveau-debutant");
        } else if (niveau.contains("intermédiaire") || niveau.contains("intermediaire")) {
            niveauLabel.getStyleClass().add("niveau-intermediaire");
        } else if (niveau.contains("avancé") || niveau.contains("avance")) {
            niveauLabel.getStyleClass().add("niveau-avance");
        }

        Label statusLabel = new Label("Statut: " + cours.getStatusCours());
        statusLabel.getStyleClass().add("cours-info");

        infoContainer.getChildren().addAll(niveauLabel, statusLabel);

        // Description avec style amélioré et gestion du débordement
        Label descriptionLabel = new Label(cours.getDescriptionCours());
        descriptionLabel.getStyleClass().add("cours-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(280);
        descriptionLabel.setMaxHeight(100);

        // Conteneur de bouton avec espacement amélioré
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));

        Button viewButton = new Button("Voir le cours");
        viewButton.getStyleClass().add("cours-button");
        viewButton.setOnAction(_ -> handleViewCours(cours));

        buttonContainer.getChildren().add(viewButton);

        // Ajout de tous les éléments à la carte
        card.getChildren().addAll(titleLabel, infoContainer, descriptionLabel, buttonContainer);
        return card;
    }

    private void handleViewCours(Cours cours) {
        // Vérifier les prérequis
        if (cours.getPrerequisCoursId() != null) {
            Cours prerequisCours = coursService.getCoursById(cours.getPrerequisCoursId());
            if (prerequisCours != null && !"lu".equals(prerequisCours.getStatusCours())) {
                showPrerequisDialog("Prérequis non complété",
                        "Vous devez d'abord compléter le cours '" + prerequisCours.getNomCours()
                                + "' avant de pouvoir accéder à ce cours.",
                        prerequisCours);
                return;
            }
        }

        // Créer une nouvelle fenêtre modale
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);

        // Créer le contenu de la fenêtre modale
        VBox contentContainer = new VBox(20);
        contentContainer.getStyleClass().add("cours-detail-container");
        contentContainer.setPadding(new Insets(30));

        // En-tête avec titre et bouton
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        headerBox.getStyleClass().add("cours-header");

        Label titleLabel = new Label(cours.getNomCours());
        titleLabel.getStyleClass().addAll("cours-detail-title", "title-text");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(600);

        // Card pour les informations du cours
        VBox infoCard = new VBox(15);
        infoCard.getStyleClass().add("info-card");
        infoCard.setPadding(new Insets(20));

        // Informations du cours dans une grille moderne
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30);
        infoGrid.setVgap(15);
        infoGrid.setPadding(new Insets(10));

        // Style commun pour les labels d'info
        String infoLabelStyle = "-fx-font-size: 14px; -fx-text-fill: #666;";
        String infoValueStyle = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";

        // Niveau
        Label niveauLabel = new Label("Niveau");
        niveauLabel.setStyle(infoLabelStyle);
        Label niveauValue = new Label(cours.getNiveau());
        niveauValue.setStyle(infoValueStyle);

        // Status
        Label statusLabel = new Label("Statut");
        statusLabel.setStyle(infoLabelStyle);
        Label statusValue = new Label(cours.getStatusCours());
        statusValue.setStyle(infoValueStyle);

        // Ajouter à la grille
        infoGrid.add(niveauLabel, 0, 0);
        infoGrid.add(niveauValue, 0, 1);
        infoGrid.add(statusLabel, 1, 0);
        infoGrid.add(statusValue, 1, 1);

        // Description avec limite de taille et expansion
        VBox descriptionBox = new VBox(10);
        Label descriptionTitle = new Label("Description");
        descriptionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea descriptionArea = new TextArea(cours.getDescriptionCours());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(4); // Limiter à 4 lignes par défaut
        descriptionArea.setMaxHeight(100); // Hauteur maximale
        descriptionArea.getStyleClass().add("description-area");

        descriptionBox.getChildren().addAll(descriptionTitle, descriptionArea);

        // Section PDF avec style amélioré
        VBox pdfSection = new VBox(15);
        pdfSection.getStyleClass().add("pdf-section");
        pdfSection.setPadding(new Insets(20));

        Label pdfTitle = new Label("Document du cours");
        pdfTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        if (cours.getPdfCours() != null && !cours.getPdfCours().isEmpty()) {
            HBox pdfActions = new HBox(15);
            pdfActions.setAlignment(Pos.CENTER_LEFT);

            Button viewPdfBtn = new Button("Voir le PDF");
            viewPdfBtn.getStyleClass().addAll("action-button", "pdf-button");

            Button translatePdfBtn = new Button("Traduire");
            translatePdfBtn.getStyleClass().addAll("action-button", "translate-button");

            Button resumeAIBtn = new Button("Résumé AI");
            resumeAIBtn.getStyleClass().addAll("action-button", "resume-ai-button");
            resumeAIBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

            // Menu pour choisir la langue de traduction
            MenuButton languageMenu = new MenuButton("Choisir la langue");
            languageMenu.getStyleClass().addAll("action-button", "language-menu");

            MenuItem englishItem = new MenuItem("English");
            MenuItem italianItem = new MenuItem("Italiano");
            MenuItem spanishItem = new MenuItem("Español");

            languageMenu.getItems().addAll(englishItem, italianItem, spanishItem);

            // Gestionnaires d'événements pour la traduction
            englishItem.setOnAction(_ -> openTranslatedPdf(cours.getPdfCours(), "en"));
            italianItem.setOnAction(_ -> openTranslatedPdf(cours.getPdfCours(), "it"));
            spanishItem.setOnAction(_ -> openTranslatedPdf(cours.getPdfCours(), "es"));

            translatePdfBtn.setOnAction(_ -> languageMenu.show());

            // Gestionnaire d'événement pour le bouton Résumé AI
            resumeAIBtn.setOnAction(_ -> openAIResume(cours.getPdfCours()));

            pdfActions.getChildren().addAll(viewPdfBtn, translatePdfBtn, resumeAIBtn, languageMenu);

            // Configuration des actions des boutons...
            viewPdfBtn.setOnAction(_ -> {
                try {
                    File pdfFile = new File("pdfs/" + cours.getPdfCours());
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    }
                } catch (Exception ex) {
                    showErrorDialog("Erreur", "Impossible d'ouvrir le PDF: " + ex.getMessage());
                }
            });

            pdfSection.getChildren().addAll(pdfTitle, pdfActions);
        } else {
            Label noPdfLabel = new Label("Aucun document PDF n'est disponible pour ce cours");
            noPdfLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            pdfSection.getChildren().addAll(pdfTitle, noPdfLabel);
        }

        // Bouton "Marquer comme lu"
        Button markAsReadButton = new Button("Marquer comme lu");
        markAsReadButton.getStyleClass().addAll("action-button", "mark-as-read-button");
        markAsReadButton.setDisable("lu".equals(cours.getStatusCours()));

        // Ajouter l'action pour le bouton "Marquer comme lu"
        markAsReadButton.setOnAction(event -> {
            // Mettre à jour le statut du cours à "lu"
            cours.setStatusCours("lu");

            // Mettre à jour le cours dans la base de données
            coursService.updateCours(cours);

            // Mettre à jour l'affichage du statut
            statusValue.setText("lu");

            // Désactiver le bouton après avoir marqué comme lu
            markAsReadButton.setDisable(true);

            // Rafraîchir la grille des cours
            updateCoursGrid();
        });

        // Bouton de fermeture stylisé
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().addAll("action-button", "close-button");
        closeButton.setOnAction(_ -> modalStage.close());

        // Container final des boutons
        HBox buttonBox = new HBox(15);
        buttonBox.getStyleClass().add("button-container");
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(markAsReadButton, closeButton);

        // Assemblage final
        infoCard.getChildren().addAll(infoGrid);
        contentContainer.getChildren().addAll(
                titleLabel,
                infoCard,
                descriptionBox,
                pdfSection,
                buttonBox);

        // Création de la scène avec effet de fond
        StackPane modalRoot = new StackPane(contentContainer);
        modalRoot.getStyleClass().add("modal-backdrop");

        Scene modalScene = new Scene(modalRoot);
        modalScene.setFill(null);

        // Ajout des styles
        modalScene.getStylesheets().addAll(
                getClass().getResource("/style_css/cours_detail_style.css").toExternalForm());

        // Configuration finale de la fenêtre
        modalStage.setScene(modalScene);
        modalStage.setMinWidth(800);
        modalStage.setMinHeight(600);
        modalStage.show();
    }

    private void openTranslatedPdf(String originalPdfName, String targetLang) {
        // Créer une fenêtre de progression
        Stage progressStage = new Stage();
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.initStyle(StageStyle.UNDECORATED);

        VBox progressBox = new VBox(15);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(20));
        progressBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        Label progressLabel = new Label("Traduction en cours...");
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        progressBox.getChildren().addAll(progressLabel, progressIndicator);

        Scene progressScene = new Scene(progressBox, 250, 150);
        progressStage.setScene(progressScene);
        progressStage.setResizable(false);

        // Lancer la traduction dans un thread séparé
        Thread translationThread = new Thread(() -> {
            try {
                // Vérifier si le fichier PDF original existe
                File originalFile = new File("pdfs/" + originalPdfName);
                if (!originalFile.exists()) {
                    Platform.runLater(() -> {
                        progressStage.close();
                        showErrorDialog("Erreur", "Le fichier PDF original n'existe pas.");
                    });
                    return;
                }

                // Extraire le texte du PDF avec PDFBox
                String textToTranslate = extractTextFromPdf(originalFile);
                if (textToTranslate == null || textToTranslate.trim().isEmpty()) {
                    Platform.runLater(() -> {
                        progressStage.close();
                        showErrorDialog("Erreur", "Impossible d'extraire le texte du PDF.");
                    });
                    return;
                }

                // Mettre à jour le label de progression
                Platform.runLater(() -> progressLabel.setText("Traduction avec MyMemory..."));

                // Traduire le texte avec MyMemory API
                String translatedText = translateWithMyMemory(textToTranslate, "fr", targetLang);

                if (translatedText != null) {
                    // Mettre à jour le label de progression
                    Platform.runLater(() -> progressLabel.setText("Création du PDF éducatif..."));

                    // Créer un PDF éducatif avec le texte original et la traduction
                    File educationalPdfFile = createEducationalPdf(textToTranslate, translatedText, targetLang,
                            originalPdfName);

                    // Créer aussi un fichier texte pour la traduction (pour compatibilité)
                    String baseName = originalPdfName.substring(0, originalPdfName.lastIndexOf('.'));
                    File translatedFile = new File("pdfs/" + baseName + "_" + targetLang + ".txt");
                    java.io.FileWriter writer = new java.io.FileWriter(translatedFile);
                    writer.write(translatedText);
                    writer.close();

                    // Fermer la fenêtre de progression et ouvrir le fichier PDF éducatif
                    Platform.runLater(() -> {
                        progressStage.close();
                        try {
                            if (Desktop.isDesktopSupported() && educationalPdfFile != null) {
                                Desktop.getDesktop().open(educationalPdfFile);
                            } else {
                                showErrorDialog("Erreur",
                                        "L'ouverture de fichier n'est pas supportée sur ce système ou le PDF n'a pas pu être créé.");
                            }
                        } catch (Exception ex) {
                            showErrorDialog("Erreur", "Impossible d'ouvrir le PDF éducatif: " + ex.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        progressStage.close();
                        showErrorDialog("Erreur de traduction",
                                "Impossible de traduire le contenu. Veuillez réessayer plus tard.");
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressStage.close();
                    showErrorDialog("Erreur", "Impossible de traduire le PDF: " + ex.getMessage());
                });
            }
        });

        // Démarrer le thread de traduction et afficher la fenêtre de progression
        translationThread.start();
        progressStage.show();
    }

    private String extractTextFromPdf(File pdfFile) {
        try {
            // Utiliser iText pour extraire le texte du PDF
            com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(pdfFile.getAbsolutePath());
            StringBuilder text = new StringBuilder();

            // Extraire le texte de chaque page
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                text.append(com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader, i));
                text.append("\n\n"); // Ajouter des sauts de ligne entre les pages
            }

            reader.close();
            return text.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, utiliser un texte de secours pour éviter un échec complet
            return "Contenu du PDF " + pdfFile.getName()
                    + ". L'extraction a échoué, mais nous allons quand même essayer de traduire ce texte.";
        }
    }

    private String translateWithMyMemory(String text, String sourceLang, String targetLang) {
        try {
            // Encoder le texte pour l'URL
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");
            String langPair = java.net.URLEncoder.encode(sourceLang + "|" + targetLang, "UTF-8");

            // Construire l'URL de l'API MyMemory
            String apiUrl = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + langPair;

            // Créer la connexion HTTP
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Analyser la réponse JSON (méthode simple sans bibliothèque JSON)
            String jsonResponse = response.toString();

            // Extraire le texte traduit (méthode simple)
            int startIndex = jsonResponse.indexOf("\"translatedText\":\"") + 18;
            int endIndex = jsonResponse.indexOf("\"", startIndex);

            if (startIndex >= 18 && endIndex > startIndex) {
                String translatedText = jsonResponse.substring(startIndex, endIndex);
                // Décoder les caractères spéciaux
                translatedText = translatedText.replace("\\\\", "\\");
                translatedText = translatedText.replace("\\n", "\n");
                translatedText = translatedText.replace("\\r", "\r");
                translatedText = translatedText.replace("\\t", "\t");
                translatedText = translatedText.replace("\\\"", "\"");
                return translatedText;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private File createEducationalPdf(String originalText, String translatedText, String targetLang, String fileName) {
        try {
            // Créer un nom de fichier pour le PDF éducatif
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            File educationalPdfFile = new File("pdfs/" + baseName + "_educational_" + targetLang + ".pdf");

            // Créer un document PDF avec iText
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(educationalPdfFile));
            document.open();

            // Ajouter un titre au document
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    18, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(41, 128, 185));
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Document éducatif bilingue",
                    titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph("\n"));

            // Ajouter une introduction
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12, com.itextpdf.text.Font.BOLD);

            String langName = "";
            switch (targetLang) {
                case "en":
                    langName = "anglais";
                    break;
                case "it":
                    langName = "italien";
                    break;
                case "es":
                    langName = "espagnol";
                    break;
                default:
                    langName = targetLang;
            }

            com.itextpdf.text.Paragraph intro = new com.itextpdf.text.Paragraph(
                    "Ce document présente le texte original en français et sa traduction en " + langName + ". " +
                            "Utilisez ce document pour comparer les deux versions et améliorer votre compréhension.",
                    normalFont);
            document.add(intro);
            document.add(new com.itextpdf.text.Paragraph("\n"));

            // Créer un tableau pour afficher le texte original et la traduction côte à côte
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);

            // En-têtes du tableau
            com.itextpdf.text.pdf.PdfPCell headerCell1 = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Paragraph("Texte original (Français)", boldFont));
            headerCell1.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
            headerCell1.setPadding(10);

            com.itextpdf.text.pdf.PdfPCell headerCell2 = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Paragraph(
                            "Traduction (" + langName.substring(0, 1).toUpperCase() + langName.substring(1) + ")",
                            boldFont));
            headerCell2.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
            headerCell2.setPadding(10);

            table.addCell(headerCell1);
            table.addCell(headerCell2);

            // Diviser le texte en paragraphes
            String[] originalParagraphs = originalText.split("\\n\\n|\n\n");
            String[] translatedParagraphs = translatedText.split("\\n\\n|\n\n");

            // Déterminer le nombre de paragraphes à afficher
            int paragraphCount = Math.min(originalParagraphs.length, translatedParagraphs.length);

            // Ajouter chaque paragraphe au tableau
            for (int i = 0; i < paragraphCount; i++) {
                com.itextpdf.text.pdf.PdfPCell cell1 = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Paragraph(originalParagraphs[i], normalFont));
                cell1.setPadding(10);

                com.itextpdf.text.pdf.PdfPCell cell2 = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Paragraph(translatedParagraphs[i], normalFont));
                cell2.setPadding(10);

                table.addCell(cell1);
                table.addCell(cell2);
            }

            document.add(table);

            // Ajouter des conseils d'apprentissage
            document.add(new com.itextpdf.text.Paragraph("\n"));
            com.itextpdf.text.Paragraph tips = new com.itextpdf.text.Paragraph("Conseils d'apprentissage:", boldFont);
            document.add(tips);

            com.itextpdf.text.List tipsList = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
            tipsList.add(new com.itextpdf.text.ListItem("Comparez les structures de phrases entre les deux langues.",
                    normalFont));
            tipsList.add(new com.itextpdf.text.ListItem("Identifiez les mots clés et leur traduction.", normalFont));
            tipsList.add(new com.itextpdf.text.ListItem(
                    "Notez les différences culturelles dans l'expression des idées.", normalFont));
            document.add(tipsList);

            // Fermer le document
            document.close();

            return educationalPdfFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showPrerequisDialog(String title, String message, Cours prerequisCours) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        VBox dialogVbox = new VBox(15);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(25));
        dialogVbox.getStyleClass().add("error-dialog");

        // En-tête avec icône éducative
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);

        // Créer une forme pour représenter une icône d'information
        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(40, 40);
        iconPane.setMaxSize(40, 40);
        iconPane.setMinSize(40, 40);

        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(20);
        circle.setFill(javafx.scene.paint.Color.valueOf("#3498db"));

        Label infoLabel = new Label("!");
        infoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        iconPane.getChildren().addAll(circle, infoLabel);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("error-title");

        headerBox.getChildren().addAll(iconPane, titleLabel);

        // Message avec style éducatif
        VBox messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(15));
        messageBox.getStyleClass().add("message-box");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.getStyleClass().add("error-message");

        // Ajouter une note éducative
        Label noteLabel = new Label(
                "Astuce: Compléter les cours dans l'ordre recommandé améliore votre apprentissage!");
        noteLabel.setWrapText(true);
        noteLabel.setMaxWidth(400);
        noteLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");

        messageBox.getChildren().addAll(messageLabel, noteLabel);

        // Conteneur pour les boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Bouton pour accéder directement au cours prérequis
        Button goToPrerequisButton = new Button("Aller au prérequis");
        goToPrerequisButton.getStyleClass().addAll("action-button", "prerequis-button");
        goToPrerequisButton.setOnAction(e -> {
            dialogStage.close();
            handleViewCours(prerequisCours);
        });

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("error-button");
        closeButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(goToPrerequisButton, closeButton);

        dialogVbox.getChildren().addAll(headerBox, messageBox, buttonBox);

        Scene dialogScene = new Scene(dialogVbox);
        dialogScene.getStylesheets().add(getClass().getResource("/style_css/cours_detail_style.css").toExternalForm());

        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        VBox dialogVbox = new VBox(15);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.getStyleClass().add("error-dialog");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("error-title");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.getStyleClass().add("error-message");

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("error-button");
        closeButton.setOnAction(e -> dialogStage.close());

        dialogVbox.getChildren().addAll(titleLabel, messageLabel, closeButton);

        Scene dialogScene = new Scene(dialogVbox);
        dialogScene.getStylesheets().add(getClass().getResource("/style_css/cours_detail_style.css").toExternalForm());

        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void openAIResume(String pdfFileName) {
        try {
            // Créer une nouvelle fenêtre modale
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UNDECORATED);
            modalStage.setTitle("Résumé AI du PDF");

            // Créer le conteneur principal
            VBox root = new VBox(20);
            root.setPadding(new Insets(0));
            root.setStyle(
                    "-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-background-radius: 15;");

            // En-tête avec dégradé de couleur éducative
            HBox header = new HBox();
            header.setPadding(new Insets(20));
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3498db, #2980b9); -fx-background-radius: 15 15 0 0;");

            // Titre avec icône éducative
            HBox titleBox = new HBox(10);
            titleBox.setAlignment(Pos.CENTER_LEFT);

            // Simuler une icône avec un cercle et un texte
            StackPane iconPane = new StackPane();
            iconPane.setMinSize(40, 40);
            iconPane.setMaxSize(40, 40);

            Circle iconCircle = new Circle(20);
            iconCircle.setFill(javafx.scene.paint.Color.WHITE);
            iconCircle.setOpacity(0.9);

            Label iconLabel = new Label("AI");
            iconLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-font-size: 16px;");

            iconPane.getChildren().addAll(iconCircle, iconLabel);

            Label titleLabel = new Label("Résumé Éducatif Intelligent");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

            titleBox.getChildren().addAll(iconPane, titleLabel);

            // Bouton de fermeture stylisé
            Button closeButton = new Button("×");
            closeButton.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 24px; -fx-cursor: hand;");
            closeButton.setOnAction(e -> modalStage.close());
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(titleBox, spacer, closeButton);

            // Zone d'information sur le PDF avec design de carte
            VBox pdfInfo = new VBox(10);
            pdfInfo.setPadding(new Insets(20));
            pdfInfo.getStyleClass().add("pdf-info");
            pdfInfo.setStyle(
                    "-fx-background-color: #f5f5f5; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-border-radius: 10px; -fx-background-radius: 10px;");

            // Titre de section avec icône
            HBox pdfTitleBox = new HBox(10);
            pdfTitleBox.setAlignment(Pos.CENTER_LEFT);

            Label pdfIcon = new Label("📄");
            pdfIcon.setStyle("-fx-font-size: 18px;");

            Label pdfTitleLabel = new Label("Document d'apprentissage");
            pdfTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 16px;");

            pdfTitleBox.getChildren().addAll(pdfIcon, pdfTitleLabel);

            Label pdfLabel = new Label("Fichier PDF : " + pdfFileName);
            pdfLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            // Ajouter une description éducative
            Label pdfDescription = new Label(
                    "Ce document sera analysé par notre intelligence artificielle pour créer un résumé adapté à vos besoins éducatifs.");
            pdfDescription.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            pdfDescription.setWrapText(true);

            pdfInfo.getChildren().addAll(pdfTitleBox, pdfLabel, pdfDescription);

            // Sélection du type de résumé avec design amélioré
            VBox typeSelectionBox = new VBox(10);
            typeSelectionBox.setPadding(new Insets(0, 20, 0, 20));

            // Titre de section avec icône
            HBox typeTitleBox = new HBox(10);
            typeTitleBox.setAlignment(Pos.CENTER_LEFT);

            Label typeIcon = new Label("🎓");
            typeIcon.setStyle("-fx-font-size: 18px;");

            Label typeTitleLabel = new Label("Personnalisation pédagogique");
            typeTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 16px;");

            typeTitleBox.getChildren().addAll(typeIcon, typeTitleLabel);

            // Description des types de résumé
            Label typeDescription = new Label(
                    "Choisissez le type de résumé qui correspond le mieux à vos besoins d'apprentissage :");
            typeDescription.setStyle("-fx-text-fill: #7f8c8d;");
            typeDescription.setWrapText(true);

            HBox typeSelection = new HBox(15);
            typeSelection.setAlignment(Pos.CENTER_LEFT);
            typeSelection.setPadding(new Insets(10, 0, 10, 0));

            Label typeLabel = new Label("Type de résumé :");
            typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            ComboBox<String> resumeTypeCombo = new ComboBox<>();
            resumeTypeCombo.getItems().addAll("Concis", "Points clés", "Détaillé", "Pour débutants", "Académique");
            resumeTypeCombo.setValue("Concis");
            resumeTypeCombo.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px;");
            HBox.setHgrow(resumeTypeCombo, Priority.ALWAYS);
            resumeTypeCombo.setPrefWidth(200);

            typeSelection.getChildren().addAll(typeLabel, resumeTypeCombo);

            // Ajouter des descriptions pour chaque type de résumé
            VBox typeDescriptionsBox = new VBox(5);
            typeDescriptionsBox.setPadding(new Insets(0, 0, 10, 0));
            typeDescriptionsBox
                    .setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px; -fx-padding: 10px;");

            Label selectedTypeDescription = new Label(
                    "Concis : Un résumé court qui présente les points essentiels du document.");
            selectedTypeDescription.setWrapText(true);
            selectedTypeDescription.setStyle("-fx-text-fill: #34495e; -fx-font-style: italic;");

            // Mettre à jour la description lorsque le type change
            resumeTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                switch (newVal) {
                    case "Concis":
                        selectedTypeDescription
                                .setText("Concis : Un résumé court qui présente les points essentiels du document.");
                        break;
                    case "Points clés":
                        selectedTypeDescription.setText(
                                "Points clés : Une liste structurée des informations importantes du document.");
                        break;
                    case "Détaillé":
                        selectedTypeDescription.setText(
                                "Détaillé : Un résumé complet avec introduction, contenu principal et conclusion.");
                        break;
                    case "Pour débutants":
                        selectedTypeDescription
                                .setText("Pour débutants : Une explication simplifiée adaptée aux apprenants novices.");
                        break;
                    case "Académique":
                        selectedTypeDescription
                                .setText("Académique : Un résumé formel avec structure et terminologie académiques.");
                        break;
                }
            });

            typeDescriptionsBox.getChildren().add(selectedTypeDescription);

            typeSelectionBox.getChildren().addAll(typeTitleBox, typeDescription, typeSelection, typeDescriptionsBox);

            // Zone de texte pour le résumé avec design amélioré
            VBox resumeBox = new VBox(10);
            resumeBox.setPadding(new Insets(0, 20, 20, 20));

            // Titre de section avec icône
            HBox resumeTitleBox = new HBox(10);
            resumeTitleBox.setAlignment(Pos.CENTER_LEFT);

            Label resumeIcon = new Label("📝");
            resumeIcon.setStyle("-fx-font-size: 18px;");

            Label resumeTitleLabel = new Label("Résumé éducatif");
            resumeTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 16px;");

            resumeTitleBox.getChildren().addAll(resumeIcon, resumeTitleLabel);

            TextArea resumeTextArea = new TextArea();
            resumeTextArea.setWrapText(true);
            resumeTextArea.setEditable(false);
            resumeTextArea.setPrefHeight(350);
            resumeTextArea.setStyle(
                    "-fx-font-size: 14px; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-background-radius: 5px;");
            VBox.setVgrow(resumeTextArea, Priority.ALWAYS);

            // Indicateur de progression et statut avec design amélioré
            HBox statusBox = new HBox(15);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(5, 0, 5, 0));

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(24, 24);
            progressIndicator.setVisible(false);

            Label statusLabel = new Label("Prêt à générer le résumé");
            statusLabel.setStyle("-fx-text-fill: #7f8c8d;");

            statusBox.getChildren().addAll(progressIndicator, statusLabel);

            // Boutons d'action avec design amélioré
            HBox actionButtons = new HBox(15);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);

            Button generateButton = new Button("Générer le résumé");
            generateButton.getStyleClass().add("action-button");
            generateButton.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px; -fx-cursor: hand;");

            Button saveButton = new Button("Sauvegarder");
            saveButton.getStyleClass().add("action-button");
            saveButton.setStyle(
                    "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px; -fx-cursor: hand;");
            saveButton.setDisable(true);

            actionButtons.getChildren().addAll(generateButton, saveButton);

            resumeBox.getChildren().addAll(resumeTitleBox, resumeTextArea, statusBox, actionButtons);

            // Ajouter tous les éléments au conteneur principal
            root.getChildren().addAll(header, pdfInfo, typeSelectionBox, resumeBox);

            // Créer la scène avec un fond semi-transparent
            StackPane modalRoot = new StackPane();
            modalRoot.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            modalRoot.getChildren().add(root);

            Scene scene = new Scene(modalRoot, 700, 800);
            scene.setFill(null);
            scene.getStylesheets().add(getClass().getResource("/style_css/cours_detail_style.css").toExternalForm());

            // Logique pour générer le résumé
            generateButton.setOnAction(e -> {
                progressIndicator.setVisible(true);
                statusLabel.setText("Génération du résumé en cours...");
                generateButton.setDisable(true);

                // Créer une tâche en arrière-plan pour extraire le texte et générer le résumé
                Task<String> task = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        // Charger le fichier PDF
                        File pdfFile = new File("pdfs/" + pdfFileName);

                        // Extraire le texte du PDF
                        PDDocument document = PDDocument.load(pdfFile);
                        PDFTextStripper stripper = new PDFTextStripper();
                        String text = stripper.getText(document);
                        document.close();

                        // Générer le résumé
                        return generateResumeFromText(text, resumeTypeCombo.getValue());
                    }
                };

                task.setOnSucceeded(event -> {
                    resumeTextArea.setText(task.getValue());
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Résumé généré avec succès");
                    generateButton.setDisable(false);
                    saveButton.setDisable(false);
                });

                task.setOnFailed(event -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Erreur lors de la génération du résumé");
                    generateButton.setDisable(false);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Impossible de générer le résumé");
                    alert.setContentText(task.getException().getMessage());
                    alert.showAndWait();
                });

                // Exécuter la tâche
                new Thread(task).start();
            });

            // Logique pour sauvegarder le résumé
            saveButton.setOnAction(e -> {
                try {
                    // Créer un nom de fichier pour le résumé
                    String baseName = pdfFileName.substring(0, pdfFileName.lastIndexOf('.'));
                    String resumeFileName = baseName + "_resume_" + resumeTypeCombo.getValue() + ".txt";

                    // Sauvegarder le résumé dans un fichier
                    File resumeFile = new File("pdfs/" + resumeFileName);
                    java.io.FileWriter writer = new java.io.FileWriter(resumeFile);
                    writer.write(resumeTextArea.getText());
                    writer.close();

                    statusLabel.setText("Résumé sauvegardé : " + resumeFileName);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sauvegarde réussie");
                    alert.setHeaderText("Le résumé a été sauvegardé");
                    alert.setContentText("Le fichier a été enregistré sous : " + resumeFileName);
                    alert.showAndWait();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Impossible de sauvegarder le résumé");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            });

            // Générer automatiquement le résumé au démarrage
            Platform.runLater(() -> generateButton.fire());

            // Configurer et afficher la fenêtre modale
            modalStage.setScene(scene);
            modalStage.showAndWait();
        } catch (Exception ex) {
            showErrorDialog("Erreur", "Impossible d'ouvrir l'outil de résumé AI : " + ex.getMessage());
        }
    }

    // Méthode pour générer un résumé à partir du texte
    private String generateResumeFromText(String text, String resumeType) {
        // Prétraitement du texte
        text = text.replaceAll("\\s+", " ").trim(); // Normaliser les espaces

        // Extraire les informations importantes (dates, délais, examens)
        Map<String, List<String>> importantInfo = extractImportantInfo(text);
        List<String> dates = importantInfo.get("dates");
        List<String> deadlines = importantInfo.get("deadlines");
        List<String> examInfo = importantInfo.get("examens");

        // Diviser le texte en phrases
        String[] sentences = text.split("[.!?]+");
        List<String> validSentences = new ArrayList<>();
        Map<String, Double> sentenceScores = new HashMap<>();

        // Filtrer les phrases vides ou trop courtes et calculer les scores
        int totalWords = 0;
        Map<String, Integer> wordFrequency = new HashMap<>();

        // Première passe: compter la fréquence des mots
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 10 && sentence.split("\\s+").length > 3) {
                validSentences.add(sentence);

                // Compter les mots pour TF-IDF
                String[] words = sentence.toLowerCase().split("\\s+");
                totalWords += words.length;

                for (String word : words) {
                    // Ignorer les mots très courts (articles, etc.)
                    if (word.length() > 3) {
                        wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }

        // Deuxième passe: calculer le score de chaque phrase (TF-IDF simplifié)
        for (String sentence : validSentences) {
            double score = 0;
            String[] words = sentence.toLowerCase().split("\\s+");

            // Calculer le score basé sur la fréquence des mots
            for (String word : words) {
                if (word.length() > 3) {
                    // Plus un mot est rare, plus il est important
                    score += 1.0 / (wordFrequency.getOrDefault(word, 1));
                }
            }

            // Normaliser par la longueur de la phrase
            score = score / words.length;

            // Bonus pour les phrases au début et à la fin (souvent plus importantes)
            int index = validSentences.indexOf(sentence);
            if (index < validSentences.size() * 0.2 || index > validSentences.size() * 0.8) {
                score *= 1.25;
            }

            // Bonus pour les phrases contenant des informations importantes
            String sentenceLower = sentence.toLowerCase();
            if (containsAnyKeyword(sentenceLower, "examen", "test", "contrôle", "évaluation", "partiel", "final")) {
                score *= 2.0; // Bonus important pour les phrases liées aux examens
            }
            if (containsAnyKeyword(sentenceLower, "date", "délai", "limite", "échéance", "rendre", "soumettre")) {
                score *= 1.8; // Bonus pour les phrases liées aux délais
            }
            if (containsAnyKeyword(sentenceLower, "important", "essentiel", "crucial", "attention", "noter",
                    "rappel")) {
                score *= 1.5; // Bonus pour les phrases d'avertissement ou de rappel
            }

            sentenceScores.put(sentence, score);
        }

        // Trier les phrases par score
        List<String> sortedSentences = new ArrayList<>(validSentences);
        sortedSentences.sort((s1, s2) -> Double.compare(sentenceScores.getOrDefault(s2, 0.0),
                sentenceScores.getOrDefault(s1, 0.0)));

        StringBuilder resumeBuilder = new StringBuilder();

        // Ajouter un en-tête en fonction du type de résumé
        resumeBuilder.append("=== Résumé ").append(resumeType).append(" ===\n\n");

        // Ajouter une section d'informations importantes si disponibles
        if (!dates.isEmpty() || !deadlines.isEmpty() || !examInfo.isEmpty()) {
            resumeBuilder.append("⚠️ INFORMATIONS IMPORTANTES ⚠️\n\n");

            if (!examInfo.isEmpty()) {
                resumeBuilder.append("📝 EXAMENS:\n");
                for (String info : examInfo) {
                    resumeBuilder.append("  • ").append(info).append("\n");
                }
                resumeBuilder.append("\n");
            }

            if (!deadlines.isEmpty()) {
                resumeBuilder.append("⏰ DÉLAIS:\n");
                for (String deadline : deadlines) {
                    resumeBuilder.append("  • ").append(deadline).append("\n");
                }
                resumeBuilder.append("\n");
            }

            if (!dates.isEmpty()) {
                resumeBuilder.append("📅 DATES IMPORTANTES:\n");
                for (String date : dates) {
                    resumeBuilder.append("  • ").append(date).append("\n");
                }
                resumeBuilder.append("\n");
            }

            resumeBuilder.append("-----------------------------------\n\n");
        }

        // Générer différents types de résumés en fonction du type sélectionné
        switch (resumeType) {
            case "Concis":
                resumeBuilder.append("Ce document contient environ ").append(totalWords)
                        .append(" mots. Voici les points essentiels:\n\n");

                // Ajouter les phrases les plus importantes (top 15%)
                int concisLimit = Math.max(3, Math.min(5, (int) (sortedSentences.size() * 0.15)));
                for (int i = 0; i < concisLimit && i < sortedSentences.size(); i++) {
                    resumeBuilder.append("• ").append(sortedSentences.get(i)).append(".\n");
                }
                break;

            case "Points clés":
                resumeBuilder.append("POINTS CLÉS DU DOCUMENT:\n\n");

                // Extraire des points clés (top 25%)
                int keyPointsLimit = Math.max(5, Math.min(10, (int) (sortedSentences.size() * 0.25)));
                for (int i = 0; i < keyPointsLimit && i < sortedSentences.size(); i++) {
                    resumeBuilder.append(i + 1).append(". ").append(sortedSentences.get(i)).append(".\n");
                }
                break;

            case "Détaillé":
                resumeBuilder.append("RÉSUMÉ DÉTAILLÉ\n\n");

                // Introduction (premières phrases importantes)
                resumeBuilder.append("Introduction:\n");
                List<String> introSentences = new ArrayList<>(
                        validSentences.subList(0, Math.min(5, validSentences.size())));
                introSentences.sort((s1, s2) -> Double.compare(sentenceScores.getOrDefault(s2, 0.0),
                        sentenceScores.getOrDefault(s1, 0.0)));
                for (int i = 0; i < Math.min(2, introSentences.size()); i++) {
                    resumeBuilder.append(introSentences.get(i)).append(". ");
                }

                // Contenu principal (phrases importantes du milieu)
                resumeBuilder.append("\n\nContenu principal:\n");
                int detailedLimit = Math.max(8, Math.min(15, (int) (sortedSentences.size() * 0.3)));
                for (int i = 0; i < detailedLimit && i < sortedSentences.size(); i++) {
                    // Éviter de répéter les phrases de l'introduction
                    if (!introSentences.subList(0, Math.min(2, introSentences.size()))
                            .contains(sortedSentences.get(i))) {
                        resumeBuilder.append(sortedSentences.get(i)).append(". ");
                    }
                }

                // Conclusion (dernières phrases importantes)
                resumeBuilder.append("\n\nConclusion:\n");
                List<String> conclusionSentences = new ArrayList<>(validSentences.subList(
                        Math.max(0, validSentences.size() - 5), validSentences.size()));
                conclusionSentences.sort((s1, s2) -> Double.compare(sentenceScores.getOrDefault(s2, 0.0),
                        sentenceScores.getOrDefault(s1, 0.0)));
                for (int i = 0; i < Math.min(2, conclusionSentences.size()); i++) {
                    resumeBuilder.append(conclusionSentences.get(i)).append(". ");
                }
                break;

            case "Pour débutants":
                resumeBuilder.append("EXPLICATION SIMPLIFIÉE\n\n");

                // Trouver la phrase d'introduction la plus importante
                String introSentence = sortedSentences.isEmpty() ? "" : sortedSentences.get(0);
                resumeBuilder.append("Ce document explique: ").append(introSentence).append(".\n\n");

                resumeBuilder.append("Concepts importants à retenir:\n");

                // Ajouter quelques concepts clés simplifiés (top 20%)
                int beginnerLimit = Math.max(4, Math.min(8, (int) (sortedSentences.size() * 0.2)));
                for (int i = 0; i < beginnerLimit && i < sortedSentences.size(); i++) {
                    // Éviter de répéter l'introduction
                    if (!sortedSentences.get(i).equals(introSentence)) {
                        resumeBuilder.append("• ").append(sortedSentences.get(i)).append(".\n");
                    }
                }

                // Ajouter un vocabulaire simplifié
                resumeBuilder.append("\nVocabulaire utile:\n");
                List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
                sortedWords.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                int vocabCount = 0;
                for (Map.Entry<String, Integer> entry : sortedWords) {
                    if (entry.getKey().length() > 4 && vocabCount < 5) {
                        resumeBuilder.append("- ").append(entry.getKey())
                                .append(": Terme important mentionné ").append(entry.getValue())
                                .append(" fois dans le document.\n");
                        vocabCount++;
                    }
                }

                resumeBuilder.append("\nContinuez à apprendre et n'hésitez pas à poser des questions!");
                break;

            case "Académique":
                resumeBuilder.append("RÉSUMÉ ACADÉMIQUE\n\n");

                // Abstract
                resumeBuilder.append("Résumé: ");
                int abstractLimit = Math.min(3, sortedSentences.size());
                for (int i = 0; i < abstractLimit; i++) {
                    resumeBuilder.append(sortedSentences.get(i)).append(". ");
                }

                // Méthodologie (simulée mais plus pertinente)
                resumeBuilder.append("\n\nMéthodologie: ");
                resumeBuilder.append("L'analyse de ce document a été réalisée en utilisant une approche d'extraction "
                        + "automatique des phrases clés basée sur leur importance sémantique. "
                        + "Le texte a été segmenté en ").append(validSentences.size())
                        .append(" unités d'information, puis analysé pour identifier les concepts centraux.");

                // Mots-clés
                resumeBuilder.append("\n\nMots-clés: ");
                List<Map.Entry<String, Integer>> academicKeywords = new ArrayList<>(wordFrequency.entrySet());
                academicKeywords.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                List<String> keywords = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : academicKeywords) {
                    if (entry.getKey().length() > 5 && keywords.size() < 7) {
                        keywords.add(entry.getKey());
                    }
                }

                resumeBuilder.append(String.join(", ", keywords));
                break;

            default:
                resumeBuilder.append("Type de résumé non reconnu. Veuillez sélectionner un type valide.");
        }

        return resumeBuilder.toString();
    }

    // Méthode pour extraire les informations importantes du texte
    private Map<String, List<String>> extractImportantInfo(String text) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<String> deadlines = new ArrayList<>();
        List<String> examInfo = new ArrayList<>();

        // Diviser le texte en phrases pour l'analyse
        String[] sentences = text.split("[.!?]+");

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() < 10)
                continue;

            String lowerSentence = sentence.toLowerCase();

            // Rechercher des informations sur les examens
            if (containsAnyKeyword(lowerSentence, "examen", "test", "contrôle", "évaluation", "partiel", "final")) {
                examInfo.add(sentence);
            }

            // Rechercher des informations sur les délais
            if (containsAnyKeyword(lowerSentence, "délai", "deadline", "limite", "échéance", "rendre", "soumettre",
                    "rendu")) {
                deadlines.add(sentence);
            }

            // Rechercher des dates (formats courants en français)
            if (lowerSentence.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*") || // 01/01/2023
                    lowerSentence.matches(
                            ".*\\d{1,2} (janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)( \\d{4})?.*")
                    || // 1 janvier 2023
                    containsAnyKeyword(lowerSentence, "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi",
                            "dimanche")) {

                // Éviter les doublons si la phrase contient à la fois une date et des mots-clés
                // d'examen/délai
                if (!examInfo.contains(sentence) && !deadlines.contains(sentence)) {
                    dates.add(sentence);
                }
            }
        }

        result.put("dates", dates);
        result.put("deadlines", deadlines);
        result.put("examens", examInfo);

        return result;
    }

    // Méthode utilitaire pour vérifier si une chaîne contient l'un des mots-clés
    private boolean containsAnyKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void handleRetourMatiereClick() {
        try {
            // Charger la vue des matières
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/user_matiere.fxml"));
            Parent root = loader.load();

            // Afficher la vue des matières
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnRetourMatiere.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du retour à la page des matières: " + e.getMessage());
            alert.showAndWait();
        }
    }
}