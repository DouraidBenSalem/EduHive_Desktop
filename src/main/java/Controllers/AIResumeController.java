package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import javafx.stage.FileChooser;

public class AIResumeController {
    @FXML
    private TextArea inputTextArea;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private Button btnGenerateResume;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> resumeTypeComboBox;
    @FXML
    private ProgressIndicator progressIndicator;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String previousResume = "";

    private boolean showContinueDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Continuer l'itération");
        alert.setHeaderText("Voulez-vous continuer à itérer?");
        alert.setContentText("Cliquez sur OK pour continuer l'itération ou sur Annuler pour terminer.");

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void generateResume() {
        if (inputTextArea.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer du texte à traiter.");
            return;
        }

        progressIndicator.setVisible(true);
        btnGenerateResume.setDisable(true);
        statusLabel.setText("Génération du résumé en cours...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // If there's a previous resume, use it as context for the next iteration
                String textToProcess = previousResume.isEmpty() ? inputTextArea.getText() : previousResume;
                return generateResumeFromText(textToProcess, resumeTypeComboBox.getValue());
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            outputTextArea.setText(result);
            previousResume = result;
            progressIndicator.setVisible(false);
            btnGenerateResume.setDisable(false);
            statusLabel.setText("Résumé généré avec succès.");

            // Show continue dialog after generation is complete
            if (showContinueDialog()) {
                // If user wants to continue, keep the current output as input for next
                // iteration
                inputTextArea.setText(result);
            } else {
                // Reset previous resume if user doesn't want to continue
                previousResume = "";
            }
        });

        task.setOnFailed(e -> {
            showAlert("Erreur", "Une erreur s'est produite lors de la génération du résumé.");
            progressIndicator.setVisible(false);
            btnGenerateResume.setDisable(false);
            statusLabel.setText("Erreur lors de la génération.");
        });

        executorService.submit(task);
    }

    private String generateResumeFromText(String text, String resumeType) {
        // Simuler un délai pour donner l'impression d'un traitement IA
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Extraire les 1000 premiers caractères pour l'analyse
        String textToAnalyze = text.length() > 1000 ? text.substring(0, 1000) : text;

        // Compter les mots
        int wordCount = textToAnalyze.split("\\s+").length;

        // Extraire quelques phrases pour le résumé
        String[] sentences = text.split("[.!?]\\s+");
        StringBuilder resumeBuilder = new StringBuilder();

        // Ajouter un en-tête en fonction du type de résumé
        resumeBuilder.append("=== Résumé ").append(resumeType).append(" ===\n\n");

        // Générer différents types de résumés en fonction du type sélectionné
        switch (resumeType) {
            case "Concis":
                resumeBuilder.append("Ce document contient environ ").append(wordCount)
                        .append(" mots dans l'extrait analysé. Voici les points essentiels:\n\n");

                // Ajouter 3-5 phrases clés
                int concisLimit = Math.min(sentences.length, 5);
                for (int i = 0; i < concisLimit; i += 2) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append("• ").append(sentences[i]).append(".\n");
                    }
                }
                break;

            case "Points clés":
                resumeBuilder.append("POINTS CLÉS DU DOCUMENT:\n\n");

                // Extraire des points clés (simulés)
                int keyPointsLimit = Math.min(sentences.length, 10);
                for (int i = 0; i < keyPointsLimit; i += 3) {
                    if (sentences[i].length() > 15) {
                        resumeBuilder.append(i / 3 + 1).append(". ").append(sentences[i]).append(".\n");
                    }
                }
                break;

            case "Détaillé":
                resumeBuilder.append("RÉSUMÉ DÉTAILLÉ\n\n");
                resumeBuilder.append("Introduction:\n");

                // Ajouter une introduction (premières phrases)
                int introLimit = Math.min(sentences.length, 3);
                for (int i = 0; i < introLimit; i++) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append(sentences[i]).append(". ");
                    }
                }

                resumeBuilder.append("\n\nContenu principal:\n");

                // Ajouter le contenu principal (phrases du milieu)
                int middleStart = Math.min(sentences.length / 4, sentences.length - 1);
                int middleEnd = Math.min(middleStart + 5, sentences.length);
                for (int i = middleStart; i < middleEnd; i++) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append(sentences[i]).append(". ");
                    }
                }

                resumeBuilder.append("\n\nConclusion:\n");

                // Ajouter une conclusion (dernières phrases)
                int conclusionStart = Math.max(sentences.length - 3, 0);
                for (int i = conclusionStart; i < sentences.length; i++) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append(sentences[i]).append(". ");
                    }
                }
                break;

            case "Pour débutants":
                resumeBuilder.append("EXPLICATION SIMPLIFIÉE\n\n");
                resumeBuilder.append("Ce document explique: ");

                // Ajouter une explication simplifiée
                if (sentences.length > 0) {
                    resumeBuilder.append(sentences[0]).append(".\n\n");
                }

                resumeBuilder.append("Concepts importants à retenir:\n");

                // Ajouter quelques concepts clés simplifiés
                int beginnerLimit = Math.min(sentences.length, 15);
                for (int i = 1; i < beginnerLimit; i += 5) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append("• ").append(sentences[i]).append(".\n");
                    }
                }

                resumeBuilder.append("\nContinuez à apprendre et n'hésitez pas à poser des questions!");
                break;

            case "Académique":
                resumeBuilder.append("RÉSUMÉ ACADÉMIQUE\n\n");
                resumeBuilder.append("Résumé: ");

                // Ajouter un résumé académique
                int academicLimit = Math.min(sentences.length, 8);
                for (int i = 0; i < academicLimit; i += 2) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append(sentences[i]).append(". ");
                    }
                }

                resumeBuilder.append("\n\nMots-clés: ");

                // Simuler l'extraction de mots-clés
                String[] words = text.split("\\s+");
                java.util.Set<String> keywords = new java.util.HashSet<>();
                for (String word : words) {
                    if (word.length() > 5 && !keywords.contains(word)) {
                        keywords.add(word);
                        if (keywords.size() >= 5)
                            break;
                    }
                }

                resumeBuilder.append(String.join(", ", keywords));

                resumeBuilder.append("\n\nRéférences: [Générées automatiquement à partir du texte source]");
                break;

            default:
                resumeBuilder.append("Résumé généré pour le texte fourni.\n");
                resumeBuilder.append("Type de résumé: ").append(resumeType).append("\n\n");

                // Ajouter un résumé par défaut
                int defaultLimit = Math.min(sentences.length, 7);
                for (int i = 0; i < defaultLimit; i += 2) {
                    if (sentences[i].length() > 10) {
                        resumeBuilder.append(sentences[i]).append(". ");
                    }
                }
        }

        // Ajouter une note de bas de page
        resumeBuilder.append("\n\n---\n");
        resumeBuilder.append("Résumé généré par EduHive AI | ").append(java.time.LocalDate.now());

        return resumeBuilder.toString();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        // Initialiser les boutons
        btnGenerateResume.setOnAction(_ -> generateResume());

        // Initialiser le bouton de sélection de fichier
        btnSelectFile.setOnAction(_ -> selectPdfFile());

        // Initialiser le bouton d'extraction de texte
        btnExtractText.setOnAction(_ -> extractTextFromPdf());

        // Initialiser le bouton de sauvegarde
        btnSaveResume.setOnAction(_ -> saveResume());

        // Initialiser le ComboBox avec les types de résumés
        resumeTypeComboBox.getItems().addAll(
                "Concis",
                "Points clés",
                "Détaillé",
                "Pour débutants",
                "Académique");
        resumeTypeComboBox.setValue("Concis");

        // Cacher l'indicateur de progression
        progressIndicator.setVisible(false);
    }

    @FXML
    private Button btnSelectFile;

    @FXML
    private Button btnExtractText;

    @FXML
    private Button btnSaveResume;

    @FXML
    private Label fileNameLabel;

    private File selectedPdfFile;

    private void selectPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File file = fileChooser.showOpenDialog(btnSelectFile.getScene().getWindow());
        if (file != null) {
            selectedPdfFile = file;
            fileNameLabel.setText(file.getName());
            statusLabel.setText("Fichier sélectionné: " + file.getName());
        }
    }

    private void extractTextFromPdf() {
        if (selectedPdfFile == null) {
            showAlert("Erreur", "Veuillez d'abord sélectionner un fichier PDF.");
            return;
        }

        initWithPdfFile(selectedPdfFile);
    }

    private void saveResume() {
        if (outputTextArea.getText().isEmpty()) {
            showAlert("Erreur", "Aucun résumé à sauvegarder.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le résumé");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));

        if (selectedPdfFile != null) {
            String suggestedName = selectedPdfFile.getName().replace(".pdf", "_resume.txt");
            fileChooser.setInitialFileName(suggestedName);
        }

        File file = fileChooser.showSaveDialog(btnSaveResume.getScene().getWindow());
        if (file != null) {
            try {
                java.nio.file.Files.writeString(file.toPath(), outputTextArea.getText());
                statusLabel.setText("Résumé sauvegardé avec succès dans " + file.getName());
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible de sauvegarder le résumé: " + ex.getMessage());
            }
        }
    }

    /**
     * Initialise le contrôleur avec un fichier PDF spécifique
     * 
     * @param pdfFile Le fichier PDF à traiter
     */
    public void initWithPdfFile(java.io.File pdfFile) {
        try {
            // Mettre à jour l'interface pour montrer le fichier sélectionné
            selectedPdfFile = pdfFile;
            fileNameLabel.setText(pdfFile.getName());

            statusLabel.setText("Extraction du texte du PDF en cours...");
            progressIndicator.setVisible(true);

            Task<String> extractTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    // Utiliser Apache PDFBox pour extraire le texte
                    org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile);
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    String text = stripper.getText(document);
                    document.close();
                    return text;
                }
            };

            extractTask.setOnSucceeded(e -> {
                String extractedText = extractTask.getValue();
                inputTextArea.setText(extractedText);
                progressIndicator.setVisible(false);
                statusLabel.setText("Texte extrait avec succès. Prêt pour la génération du résumé.");

                // Générer automatiquement le résumé après l'extraction du texte
                generateResume();
            });

            extractTask.setOnFailed(e -> {
                showAlert("Erreur",
                        "Impossible d'extraire le texte du PDF: " + extractTask.getException().getMessage());
                progressIndicator.setVisible(false);
                statusLabel.setText("Erreur lors de l'extraction du texte.");
            });

            executorService.submit(extractTask);
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'initialiser avec le fichier PDF: " + ex.getMessage());
            statusLabel.setText("Erreur lors de l'initialisation.");
        }
    }
}