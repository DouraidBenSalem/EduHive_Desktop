package Controllers;

import Entities.quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import Services.QuizService;
import Services.QuizServiceImpl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;

public class quizcontroller {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private Button Resultatpage;

    @FXML
    private ListView<quiz> quizztable;

    private ObservableList<quiz> quizList = FXCollections.observableArrayList();
    
    // Add the service
    // Update the service instantiation
    private QuizService quizService = new QuizServiceImpl();
    
    @FXML
    void initialize() {
        navbarController.setParent(this);
        
        loadQuizFromDB();
        
        // Configure ListView cell factory to display quiz information
        quizztable.setCellFactory(new Callback<ListView<quiz>, ListCell<quiz>>() {
            @Override
            public ListCell<quiz> call(ListView<quiz> param) {
                return new ListCell<quiz>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox buttons = new HBox(10, btnEdit, btnDelete);
                    
                    @Override
                    protected void updateItem(quiz item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Format the quiz information for display
                            setText(
                                   " | Titre: " + item.getTitre() + 
                                   " | Question: " + item.getQuestion() + 
                                   " | Réponse correcte: " + item.getRepCorrect());
                            
                            // Configure edit button
                            btnEdit.setOnAction(event -> {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ajouterquiz.fxml"));
                                    Scene scene = new Scene(loader.load());
                                    
                                    add_quiz_controller controller = loader.getController();
                                    controller.initData(item);
                                    controller.setOnSaveCallback(() -> loadQuizFromDB());
                                    
                                    Stage stage = new Stage();
                                    stage.setTitle("Modifier un Quiz");
                                    stage.setScene(scene);
                                    stage.show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            
                            // Configure delete button
                            btnDelete.setOnAction(event -> {
                                deleteQuiz(item.getId());
                                loadQuizFromDB();
                            });
                            
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    private void loadQuizFromDB() {
        quizList.clear();
        // Use the service instead of direct database access
        quizList.addAll(quizService.getAllQuizzes());
        quizztable.setItems(quizList);
    }

    private void deleteQuiz(int id) {
        // Use the service instead of direct database access
        quizService.deleteQuiz(id);
        System.out.println("Quiz supprimé avec succès.");
    }

    // Method removed as functionality is now integrated in ListView cell factory


    @FXML
    void ajoutertable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajouterquiz.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Quiz");
            stage.setScene(new Scene(loader.load()));


            add_quiz_controller controller = loader.getController();

            controller.setOnSaveCallback(() -> loadQuizFromDB());

            stage.show();

            stage.setOnHidden(e -> loadQuizFromDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exportertable(ActionEvent event) {
        try {

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save PDF File");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            java.io.File file = fileChooser.showSaveDialog(source.getScene().getWindow());

            if (file != null) {

                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();


                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Quiz List", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Add space


                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(6); // 6 columns
                pdfTable.setWidthPercentage(100);


                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"ID", "Titre", "Question", "Réponse Correcte", "Option A", "Option B"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }


                for (quiz q : quizList) {
                    pdfTable.addCell(String.valueOf(q.getId()));
                    pdfTable.addCell(q.getTitre());
                    pdfTable.addCell(q.getQuestion());
                    pdfTable.addCell(q.getRepCorrect());
                    pdfTable.addCell(q.getOptionA());
                    pdfTable.addCell(q.getOptionB());
                }

                document.add(pdfTable);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Quiz data has been exported to PDF successfully!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while exporting to PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    void navigateresultat(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("resultatpage.fxml"));
            Scene scene = new Scene(loader.load());


            Stage newStage = new Stage();
            newStage.setTitle("Résultats");
            newStage.setScene(scene);
            newStage.show();


            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
