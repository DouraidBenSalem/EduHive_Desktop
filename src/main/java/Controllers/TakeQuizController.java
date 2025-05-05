package Controllers;

import Entities.Result;
import Entities.quiz;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import Services.QuizService;
import Services.QuizServiceImpl;
import Services.ResultService;
import Services.ResultServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeQuizController {

    @FXML
    private frontnavcontrollers navbarController;

    @FXML
    private Label quizTitle;

    @FXML
    private VBox quizContainer;

    @FXML
    private VBox questionTemplate;

    @FXML
    private Button submitButton;
    
    @FXML
    private Button allQuizzesButton;

    @FXML
    private AnchorPane resultContainer;

    @FXML
    private Label resultTitle;

    @FXML
    private Rectangle scoreCircle;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label correctAnswersLabel;

    @FXML
    private Label incorrectAnswersLabel;

    @FXML
    private Label commentLabel;

    @FXML
    private Button finishButton;

    private QuizService quizService = new QuizServiceImpl();
    private ResultService resultService = new ResultServiceImpl();
    private quiz currentQuiz;
    private List<quiz> quizQuestions = new ArrayList<>();
    private Map<Integer, ToggleGroup> answerGroups = new HashMap<>();
    private int userId = 1; 
    private String userName = "Utilisateur"; 

    @FXML
    void initialize() {
      
        questionTemplate.setVisible(false);
        questionTemplate.setManaged(false);

        loadQuizQuestions();
    }

    /**
     * @param quizId 
     */
    public void initData(int quizId) {
        currentQuiz = quizService.getQuizById(quizId);
        if (currentQuiz != null) {
            quizTitle.setText(currentQuiz.getTitre());
            loadQuizQuestions(quizId);
        }
    }


    private void loadQuizQuestions() {
        quizQuestions = quizService.getAllQuizzes();
        displayQuizQuestions();
    }
    

    public void loadAllQuizzes() {
        quizTitle.setText("Tous les Quiz Disponibles");
        quizQuestions = quizService.getAllQuizzes();
        displayQuizQuestions();
    }

    /**
     * @param quizId
     */
    private void loadQuizQuestions(int quizId) {

        quizQuestions.clear();
        quiz q = quizService.getQuizById(quizId);
        if (q != null) {
            quizQuestions.add(q);
            displayQuizQuestions();
        }
    }

 
    private void displayQuizQuestions() {

        quizContainer.getChildren().clear();
        quizContainer.getChildren().add(quizTitle);
        for (int i = 0; i < quizQuestions.size(); i++) {
            quiz q = quizQuestions.get(i);
            VBox questionBox = createQuestionBox(q, i);
            quizContainer.getChildren().add(questionBox);
        }
    }

    private VBox createQuestionBox(quiz q, int index) {

        VBox questionBox = new VBox();
        questionBox.getStyleClass().add("question-container");
        questionBox.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        questionBox.setSpacing(10);
        questionBox.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e0e0e0;");


        Label questionText = new Label((index + 1) + ". " + q.getQuestion());
        questionText.getStyleClass().add("question-text");
        questionText.setFont(new javafx.scene.text.Font("System Bold", 16));
        questionText.setStyle("-fx-text-fill: #3f51b5;");
        questionBox.getChildren().add(questionText);


        VBox optionsContainer = new VBox();
        optionsContainer.setSpacing(10);
        optionsContainer.setPadding(new javafx.geometry.Insets(10, 0, 0, 20));


        ToggleGroup answerGroup = new ToggleGroup();
        answerGroups.put(index, answerGroup);


        HBox optionABox = new HBox(10);
        RadioButton optionA = new RadioButton(q.getOptionA());
        optionA.setToggleGroup(answerGroup);
        optionA.setFont(new javafx.scene.text.Font(14));
        optionA.setUserData(q.getOptionA());
        optionA.setStyle("-fx-text-fill: #555555;");
        optionABox.getChildren().add(optionA);

        HBox optionBBox = new HBox(10);
        RadioButton optionB = new RadioButton(q.getOptionB());
        optionB.setToggleGroup(answerGroup);
        optionB.setFont(new javafx.scene.text.Font(14));
        optionB.setUserData(q.getOptionB());
        optionB.setStyle("-fx-text-fill: #555555;");
        optionBBox.getChildren().add(optionB);

        HBox optionCorrectBox = new HBox(10);
        RadioButton optionCorrect = new RadioButton(q.getRepCorrect());
        optionCorrect.setToggleGroup(answerGroup);
        optionCorrect.setFont(new javafx.scene.text.Font(14));
        optionCorrect.setUserData(q.getRepCorrect());
        optionCorrect.setStyle("-fx-text-fill: #555555;");
        
  
        if (!q.getRepCorrect().equals(q.getOptionA()) && !q.getRepCorrect().equals(q.getOptionB())) {
            optionCorrectBox.getChildren().addAll(optionCorrect);
        } else {
           
            if (q.getOptionA().equals(q.getRepCorrect())) {
                Label correctLabelA = new Label(" (Réponse correcte)");
                correctLabelA.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                optionABox.getChildren().add(correctLabelA);
            }
            if (q.getOptionB().equals(q.getRepCorrect())) {
                Label correctLabelB = new Label(" (Réponse correcte)");
                correctLabelB.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                optionBBox.getChildren().add(correctLabelB);
            }
        }

    
if (!q.getRepCorrect().equals(q.getOptionA()) && !q.getRepCorrect().equals(q.getOptionB())) {
    optionsContainer.getChildren().addAll(optionABox, optionBBox, optionCorrectBox);
} else {
    optionsContainer.getChildren().addAll(optionABox, optionBBox);
}
        questionBox.getChildren().add(optionsContainer);

        return questionBox;
    }


    @FXML
    void handleSubmit(ActionEvent event) {
        if (!validateAllQuestionsAnswered()) {
            showAlert("Attention", "Veuillez répondre à toutes les questions avant de soumettre.");
            return;
        }

        int correctAnswers = 0;
        int incorrectAnswers = 0;

        for (int i = 0; i < quizQuestions.size(); i++) {
            quiz q = quizQuestions.get(i);
            ToggleGroup group = answerGroups.get(i);
            
            if (group.getSelectedToggle() != null) {
                String selectedAnswer = group.getSelectedToggle().getUserData().toString();
                if (selectedAnswer.equals(q.getRepCorrect())) {
                    correctAnswers++;
                    showQuestionResult(i, true);
                } else {
                    incorrectAnswers++;
                    showQuestionResult(i, false);
                }
            } else {
                incorrectAnswers++;
            }
        }

        double totalQuestions = quizQuestions.size();
        int note = (int) Math.round((correctAnswers / totalQuestions) * 20);

        String commentaire = generateComment(note);

        displayResults(note, correctAnswers, incorrectAnswers, commentaire);

        saveResult(note, commentaire, correctAnswers, incorrectAnswers);

        utils.EmailService.sendQuizResultEmail(
            "douraid.bensalem@gmail.com",
            userName,
            note,
            correctAnswers,
            (int)totalQuestions
        );

        showAlert("Succès", "L'email a été envoyé avec succès!");

        Label autoCloseLabel = new Label("Les résultats se fermeront automatiquement dans 6 secondes...");
        autoCloseLabel.setStyle("-fx-text-fill: #555555; -fx-font-style: italic;");
        resultContainer.getChildren().add(autoCloseLabel);
        
        finishButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(6));
        delay.setOnFinished(e -> {
            resultContainer.setVisible(false);
            
            resetQuizInterface();
        });
        delay.play();
        

    }

    /**
     * @return true si toutes les questions ont été répondues, false sinon
     */
    private boolean validateAllQuestionsAnswered() {
        for (ToggleGroup group : answerGroups.values()) {
            if (group.getSelectedToggle() == null) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param questionIndex 
     * @param isCorrect
     */
    private void showQuestionResult(int questionIndex, boolean isCorrect) {

        VBox questionBox = (VBox) quizContainer.getChildren().get(questionIndex + 1); 
       
        if (isCorrect) {
            questionBox.setStyle(questionBox.getStyle() + "; -fx-border-color: #4CAF50; -fx-border-width: 2px;");
          
            Label correctLabel = new Label("✓ Correct!");
            correctLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            questionBox.getChildren().add(correctLabel);
        } else {
            questionBox.setStyle(questionBox.getStyle() + "; -fx-border-color: #F44336; -fx-border-width: 2px;");
            
            quiz q = quizQuestions.get(questionIndex);
            Label incorrectLabel = new Label("✗ Incorrect! La réponse correcte est: " + q.getRepCorrect());
            incorrectLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            questionBox.getChildren().add(incorrectLabel);
        }
    }

    /**
     * @param note 
     * @return 
     */
    private String generateComment(int note) {
        if (note >= 16) {
            return "Excellent travail ! Vous avez une très bonne compréhension des concepts.";
        } else if (note >= 12) {
            return "Bon travail ! Vous avez une bonne compréhension des concepts.";
        } else if (note >= 8) {
            return "Vous avez une compréhension moyenne des concepts. Continuez à travailler !";
        } else if (note >= 4) {
            return "Vous avez besoin de revoir les concepts. N'hésitez pas à demander de l'aide.";
        } else {
            return "Vous avez besoin de beaucoup plus de travail sur ces concepts. Consultez votre enseignant.";
        }
    }

    /**
     * @param note 
     * @param correctAnswers 
     * @param incorrectAnswers 
     * @param commentaire
     */
    private void displayResults(int note, int correctAnswers, int incorrectAnswers, String commentaire) {
        
        scoreLabel.setText(note + "/20");
        correctAnswersLabel.setText(String.valueOf(correctAnswers));
        incorrectAnswersLabel.setText(String.valueOf(incorrectAnswers));
        commentLabel.setText(commentaire);

    
        if (note >= 16) {
            scoreCircle.setFill(Color.web("#4CAF50"));
        } else if (note >= 10) {
            scoreCircle.setFill(Color.web("#FFC107"));
        } else {
            scoreCircle.setFill(Color.web("#F44336")); 
        }

     
        resultContainer.setVisible(true);
        resultContainer.setOpacity(0);

      
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), resultContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

     
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.5), scoreCircle);
        scaleUp.setFromX(0);
        scaleUp.setFromY(0);
        scaleUp.setToX(1);
        scaleUp.setToY(1);

        SequentialTransition sequentialTransition = new SequentialTransition(fadeIn, scaleUp);
        sequentialTransition.play();
    }

    /**
     * 
     * @param note
     * @param commentaire 
     * @param correctAnswers
     * @param incorrectAnswers 
     */
    private void saveResult(int note, String commentaire, int correctAnswers, int incorrectAnswers) {
       
        Result result = new Result();
        result.setUserId(userId);
        result.setUserName(userName);
        result.setNote(note);
        result.setCommentaire(commentaire);
        result.setNbRepCorrect(correctAnswers);
        result.setNbRepIncorrect(incorrectAnswers);
        

        if (currentQuiz != null) {
            result.setQuizId(currentQuiz.getId());
            result.setQuizTitle(currentQuiz.getTitre());
        }
        
        resultService.addResult(result);
    }

  
    @FXML
    void handleFinish(ActionEvent event) {
    
        resultContainer.setVisible(false);
        
      
        resetQuizInterface();
    }
    
 
    private void resetQuizInterface() {
      
        answerGroups.clear();

        if (currentQuiz != null) {
            loadQuizQuestions(currentQuiz.getId());
        } else {
            loadQuizQuestions();
        }

    }
    
    @FXML
    void handleAIQuizGenerator(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/ai_quiz_generator.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation vers le générateur de quiz IA: " + e.getMessage());
        }
    }
    


    /**
     * @param title 
     * @param message 
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
