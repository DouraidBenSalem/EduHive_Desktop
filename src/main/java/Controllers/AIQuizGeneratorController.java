package Controllers;

import Entities.quiz;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuizService;
import services.QuizServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIQuizGeneratorController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private TextField topicInput;

    @FXML
    private Button generateButton;

    @FXML
    private Label loadingLabel;

    @FXML
    private VBox quizResultContainer;

    @FXML
    private Button saveQuizButton;

    @FXML
    private Button backButton;

    private QuizService quizService = new QuizServiceImpl();
    private List<quiz> generatedQuizzes = new ArrayList<>();
    private static final String API_KEY = "AIzaSyBKyv3-HyNEH0obb9dZy7oBs_6gK50Su2E";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    @FXML
    void initialize() {
        // Initialisation du contrôleur
    }

    @FXML
    void handleGenerateQuiz(ActionEvent event) {
        String topic = topicInput.getText().trim();
        if (topic.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un sujet pour générer un quiz.");
            return;
        }

        // Réinitialiser l'interface
        quizResultContainer.getChildren().clear();
        generatedQuizzes.clear();
        saveQuizButton.setDisable(true);
        loadingLabel.setVisible(true);
        generateButton.setDisable(true);

        // Créer une tâche en arrière-plan pour l'appel API
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return generateQuizFromAPI(topic);
            }
        };

        task.setOnSucceeded(e -> {
            String apiResponse = task.getValue();
            processQuizResponse(apiResponse);
            loadingLabel.setVisible(false);
            generateButton.setDisable(false);
            saveQuizButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            loadingLabel.setVisible(false);
            generateButton.setDisable(false);
            showAlert("Erreur", "Erreur lors de la génération du quiz: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private String generateQuizFromAPI(String topic) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String systemContext = "You are a helpful quiz generator. When users provide a topic, " +
                    "generate engaging multiple-choice questions. Format each question with:\n" +
                    "[Question Number]. [Question Text]\n" +
                    "A) [First option]\n" +
                    "B) [Second option]\n" +
                    "C) [Third option] (Correct Answer)\n\n" +
                    "Correct Answer: [The actual correct answer]\n\n" +
                    "- ALWAYS ensure that Option C is the correct answer.\n" +
                    "- Do NOT explicitly write 'Correct Answer:' — just list options A, B, and C.\n" +
                    "- Separate each question by a blank line.";

            String requestBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"role\": \"user\",\n" +
                    "    \"parts\": [{\n" +
                    "      \"text\": \"" + systemContext + "\n\nGenerate a quiz about: " + topic + "\"\n" +
                    "    }]\n" +
                    "  }]\n" +
                    "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return extractTextFromResponse(response.toString());
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    throw new IOException("API Error: " + response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur: " + e.getMessage();
        }
    }
    private String extractTextFromResponse(String jsonResponse) {
        try {
            System.out.println("Réponse JSON brute: " + jsonResponse);
    
            // Corriger le modèle regex pour extraire le champ text correctement
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(jsonResponse);
    
            if (matcher.find()) {
                String text = matcher.group(1);
                String processedText = text
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\t", "\t")
                        .replace("\\r", "");
    
                System.out.println("Texte extrait et traité: " + processedText);
                return processedText;
            } else {
                System.out.println("Aucun champ 'text' trouvé dans la réponse JSON");
                return "Aucune question valide trouvée dans la réponse. Veuillez réessayer.";
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'extraction de la réponse: " + e.getMessage());
            return "Erreur lors du traitement de la réponse: " + e.getMessage();
        }
    }
    
    
    private void processQuizResponse(String apiResponse) {
        // Afficher la réponse complète pour le débogage
        System.out.println("Réponse API complète: " + apiResponse);
        
        // Vérifier si la réponse contient une erreur
        if (apiResponse.startsWith("Erreur:") || apiResponse.contains("Aucune question valide")) {
            Platform.runLater(() -> {
                Label errorLabel = new Label("Erreur API: " + apiResponse);
                errorLabel.setStyle("-fx-text-fill: red;");
                quizResultContainer.getChildren().add(errorLabel);
            });
            return;
        }
        
        // Diviser la réponse en questions individuelles en utilisant un pattern plus strict
        String[] questions = apiResponse.split("\n\n+");
        System.out.println("Nombre de segments après division: " + questions.length);
        
        boolean anyQuestionsGenerated = false;
        
        for (int i = 0; i < questions.length; i++) {
            String questionText = questions[i].trim();
            System.out.println("Segment " + (i+1) + ": " + questionText);
            
            // Vérifier si le segment correspond au format attendu d'une question
            if (!questionText.matches("(?s)\\d+\\.\\s*.*\\nA\\).*\\nB\\).*\\nC\\).*")) {
                System.out.println("Segment ignoré: ne correspond pas au format de question");
                continue;
            }

            try {
                // Extraire les parties de la question avec des patterns plus robustes
                Pattern questionPattern = Pattern.compile("\\d+\\.\\s*([^\n]+)");
                Pattern optionAPattern = Pattern.compile("A\\)\\s*([^\n]+)");
                Pattern optionBPattern = Pattern.compile("B\\)\\s*([^\n]+)");
                Pattern optionCPattern = Pattern.compile("C\\)\\s*([^\n]+)");
                
                // Il n'y aura pas de correctAnswerPattern ici
                
                Matcher questionMatcher = questionPattern.matcher(questionText);
                Matcher optionAMatcher = optionAPattern.matcher(questionText);
                Matcher optionBMatcher = optionBPattern.matcher(questionText);
                Matcher optionCMatcher = optionCPattern.matcher(questionText);
                
                boolean hasQuestion = questionMatcher.find();
                boolean hasOptionA = optionAMatcher.find();
                boolean hasOptionB = optionBMatcher.find();
                boolean hasOptionC = optionCMatcher.find();
                
                if (hasQuestion && hasOptionA && hasOptionB && hasOptionC) {
                    questionMatcher.reset();
                    optionAMatcher.reset();
                    optionBMatcher.reset();
                    optionCMatcher.reset();
                
                    questionMatcher.find();
                    optionAMatcher.find();
                    optionBMatcher.find();
                    optionCMatcher.find();
                
                    String question = questionMatcher.group(1).trim();
                    String optionA = optionAMatcher.group(1).trim();
                    String optionB = optionBMatcher.group(1).trim();
                    String optionC = optionCMatcher.group(1).trim();
                
                    quiz newQuiz = new quiz();
                    newQuiz.setTitre("Question générée par IA");
                    newQuiz.setQuestion(question);
                    newQuiz.setOptionA(optionA);
                    newQuiz.setOptionB(optionB);
                    newQuiz.setRepCorrect(optionC); // Option C est la bonne réponse
                
                    generatedQuizzes.add(newQuiz);
                    anyQuestionsGenerated = true;
                
                    Platform.runLater(() -> {
                        displayQuizQuestion(newQuiz);
                    });
                
                
            }
            } catch (Exception e) {
                System.out.println("Erreur lors de l'extraction de la question: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!anyQuestionsGenerated) {
            System.out.println("Aucune question n'a pu être générée");
            Platform.runLater(() -> {
                Label errorLabel = new Label("Aucune question n'a pu être générée. Veuillez réessayer.");
                errorLabel.setStyle("-fx-text-fill: red;");
                quizResultContainer.getChildren().add(errorLabel);
                
                // Ajouter des informations de débogage dans l'interface
                if (apiResponse.length() > 0) {
                    Label debugLabel = new Label("Réponse API reçue mais format incorrect. Essayez un sujet plus spécifique.");
                    debugLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
                    debugLabel.setWrapText(true);
                    quizResultContainer.getChildren().add(debugLabel);
                }
            });
        }
    }

    private void displayQuizQuestion(quiz q) {
        VBox questionBox = new VBox();
        questionBox.getStyleClass().add("question-container");
        questionBox.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        questionBox.setSpacing(10);
        questionBox.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0;");
    
        Label questionText = new Label(q.getQuestion());
        questionText.getStyleClass().add("question-text");
        questionText.setFont(new javafx.scene.text.Font("System Bold", 16));
        questionText.setStyle("-fx-text-fill: #3f51b5;");
        questionText.setWrapText(true);
    
        Label optionALabel = new Label("A) " + q.getOptionA());
        optionALabel.setFont(new javafx.scene.text.Font(14));
        optionALabel.setStyle("-fx-text-fill: #555555;");
        optionALabel.setWrapText(true);
    
        Label optionBLabel = new Label("B) " + q.getOptionB());
        optionBLabel.setFont(new javafx.scene.text.Font(14));
        optionBLabel.setStyle("-fx-text-fill: #555555;");
        optionBLabel.setWrapText(true);

        Label optionCLabel = new Label("C) " + q.getRepCorrect());
        optionBLabel.setFont(new javafx.scene.text.Font(14));
        optionBLabel.setStyle("-fx-text-fill: #555555;");
        optionBLabel.setWrapText(true);
    
        // Now displaying the Correct Answer BELOW options, with different color
        Label correctAnswerLabel = new Label("Réponse correcte: " + q.getRepCorrect());
        correctAnswerLabel.setFont(new javafx.scene.text.Font(14));
        correctAnswerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;"); // Green color
        correctAnswerLabel.setWrapText(true);
    
        // Add in correct order: Question, Options A and B, then Correct Answer
        questionBox.getChildren().addAll(questionText, optionALabel, optionBLabel,optionCLabel, correctAnswerLabel);
        quizResultContainer.getChildren().add(questionBox);
    }
    

    @FXML
    void handleSaveQuiz(ActionEvent event) {
        if (generatedQuizzes.isEmpty()) {
            showAlert("Erreur", "Aucun quiz à enregistrer.");
            return;
        }

        int savedCount = 0;
        for (quiz q : generatedQuizzes) {
            try {
                quizService.addQuiz(q);
                savedCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (savedCount > 0) {
            showAlert("Succès", savedCount + " questions ont été enregistrées avec succès!");
            // Réinitialiser l'interface après l'enregistrement
            quizResultContainer.getChildren().clear();
            generatedQuizzes.clear();
            saveQuizButton.setDisable(true);
            topicInput.clear();
        } else {
            showAlert("Erreur", "Aucune question n'a pu être enregistrée.");
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/take_quiz.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour à la page précédente: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}