package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import Entities.quiz;
import Services.QuizService;
import Services.QuizServiceImpl;
import javafx.stage.Stage;

public class add_quiz_controller {

    @FXML private Button ajouter;
    @FXML private Button effacer;
    @FXML private Button exit;
    @FXML private TextField option_a;
    @FXML private TextField option_b;
    @FXML private TextField question;
    @FXML private TextField rep_correct;
    @FXML private TextField titre;

    @FXML private Label titreErrorLabel;
    @FXML private Label questionErrorLabel;
    @FXML private Label repCorrectErrorLabel;
    @FXML private Label optionAErrorLabel;
    @FXML private Label optionBErrorLabel;

    private boolean isEditMode = false;
    private quiz quizToEdit;
    private Runnable onSaveCallback;

    private QuizService quizService = new QuizServiceImpl();

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(quiz q) {
        if (q != null) {
            this.quizToEdit = q;
            this.isEditMode = true;
            ajouter.setText("Modifier");

            titre.setText(q.getTitre());
            question.setText(q.getQuestion());
            rep_correct.setText(q.getRepCorrect());
            option_a.setText(q.getOptionA());
            option_b.setText(q.getOptionB());
        }
    }

    @FXML
    void initialize() {
        // Ajouter des listeners pour la validation en temps réel
        titre.textProperty().addListener((observable, oldValue, newValue) -> validateTitreField());
        question.textProperty().addListener((observable, oldValue, newValue) -> validateQuestionField());
        rep_correct.textProperty().addListener((observable, oldValue, newValue) -> validateRepCorrectField());
        option_a.textProperty().addListener((observable, oldValue, newValue) -> validateOptionAField());
        option_b.textProperty().addListener((observable, oldValue, newValue) -> validateOptionBField());
    }

    private boolean validateTitreField() {
        String text = titre.getText().trim();
        if (text.isEmpty()) {
            titreErrorLabel.setText("Le titre ne peut pas être vide");
            titreErrorLabel.setVisible(true);
            titreErrorLabel.setManaged(true);
            return false;
        }
 
        titreErrorLabel.setVisible(false);
        titreErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateQuestionField() {
        String text = question.getText().trim();
        if (text.isEmpty()) {
            questionErrorLabel.setText("La question ne peut pas être vide");
            questionErrorLabel.setVisible(true);
            questionErrorLabel.setManaged(true);
            return false;
        }
        // Vérifier que la question se termine par un point d'interrogation
        if (!text.endsWith("?")) {
            questionErrorLabel.setText("La question doit se terminer par un point d'interrogation");
            questionErrorLabel.setVisible(true);
            questionErrorLabel.setManaged(true);
            return false;
        }
        questionErrorLabel.setVisible(false);
        questionErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateRepCorrectField() {
        String text = rep_correct.getText().trim();
        if (text.isEmpty()) {
            repCorrectErrorLabel.setText("La réponse correcte ne peut pas être vide");
            repCorrectErrorLabel.setVisible(true);
            repCorrectErrorLabel.setManaged(true);
            return false;
        }
      
        repCorrectErrorLabel.setVisible(false);
        repCorrectErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateOptionAField() {
        String text = option_a.getText().trim();
        if (text.isEmpty()) {
            optionAErrorLabel.setText("L'option A ne peut pas être vide");
            optionAErrorLabel.setVisible(true);
            optionAErrorLabel.setManaged(true);
            return false;
        }
        // Vérifier que l'option A est de type texte (pas uniquement des chiffres)
   
        optionAErrorLabel.setVisible(false);
        optionAErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateOptionBField() {
        String text = option_b.getText().trim();
        if (text.isEmpty()) {
            optionBErrorLabel.setText("L'option B ne peut pas être vide");
            optionBErrorLabel.setVisible(true);
            optionBErrorLabel.setManaged(true);
            return false;
        }
        // Vérifier que l'option B est de type texte (pas uniquement des chiffres)

        optionBErrorLabel.setVisible(false);
        optionBErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateAllFields() {
        // Vérifier tous les champs et bloquer la soumission si nécessaire
        boolean titreValid = validateTitreField();
        boolean questionValid = validateQuestionField();
        boolean repCorrectValid = validateRepCorrectField();
        boolean optionAValid = validateOptionAField();
        boolean optionBValid = validateOptionBField();

        // Retourner true seulement si tous les champs sont valides
        return titreValid && questionValid && repCorrectValid && optionAValid && optionBValid;
    }

    @FXML
    void ajouterquiz(ActionEvent event) {
        // Vérifier les champs et bloquer la soumission si nécessaire
        if (!validateAllFields()) {
            return;
        }

        String t = titre.getText();
        String q = question.getText();
        String r = rep_correct.getText();
        String a = option_a.getText();
        String b = option_b.getText();

        try {
            if (isEditMode && quizToEdit != null) {

                quiz updatedQuiz = new quiz(
                        quizToEdit.getId(),
                        t,
                        q,
                        r,
                        a,
                        b
                );
                quizService.updateQuiz(updatedQuiz);
                showAlert("Quiz modifié avec succès !");
            } else {

                quiz newQuiz = new quiz(
                    0,
                    t,
                    q,
                    r,
                    a,
                    b
                );
                quizService.addQuiz(newQuiz);
                showAlert("Quiz ajouté avec succès !");
            }

            if (onSaveCallback != null) onSaveCallback.run();


            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        // Suppression des alertes comme demandé
        System.out.println(msg);
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    void effacerquiz(ActionEvent event) {
        titre.clear();
        question.clear();
        rep_correct.clear();
        option_a.clear();
        option_b.clear();
    }
}
