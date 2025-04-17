package Controllers;

import Entities.Result;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import Services.ResultService;
import Services.ResultServiceImpl;

import java.io.IOException;

public class ResultController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporterBtn;

    @FXML
    private Button quizPageBtn;

    @FXML
    private ListView<Result> resultTable;

    private ObservableList<Result> resultList = FXCollections.observableArrayList();
    
    private ResultService resultService = new ResultServiceImpl();
    
    @FXML
    void initialize() {
        if (navbarController != null) {

        }
        
        loadResultsFromDB();
        
        // Configure ListView cell factory to display result information
        resultTable.setCellFactory(new Callback<ListView<Result>, ListCell<Result>>() {
            @Override
            public ListCell<Result> call(ListView<Result> param) {
                return new ListCell<Result>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox buttons = new HBox(10, btnEdit, btnDelete);
                    
                    @Override
                    protected void updateItem(Result item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Format the result information for display
                            setText(
                                   " | User ID: " + item.getUserId() + 
                                   " | Quiz ID: " + item.getQuizId() + 
                                   " | Note: " + item.getNote() +
                                           " | nb_rep_correct: " + item.getNbRepCorrect() +
                                           " | nb_rep_incorrect: " + item.getNbRepIncorrect() +
                                           " | Commentaire: " + item.getCommentaire());
                            
                            // Configure edit button
                            btnEdit.setOnAction(event -> {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("add_result.fxml"));
                                    Scene scene = new Scene(loader.load());
                                    
                                    AddResultController controller = loader.getController();
                                    controller.initData(item);
                                    controller.setOnSaveCallback(() -> loadResultsFromDB());
                                    
                                    Stage stage = new Stage();
                                    stage.setTitle("Modifier un Résultat");
                                    stage.setScene(scene);
                                    stage.show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            
                            // Configure delete button
                            btnDelete.setOnAction(event -> {
                                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                                confirmDialog.setTitle("Confirmation de suppression");
                                confirmDialog.setHeaderText("Supprimer le résultat");
                                confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce résultat?");
                                
                                confirmDialog.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        deleteResult(item.getId());
                                        loadResultsFromDB();
                                    }
                                });
                            });
                            
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    private void loadResultsFromDB() {
        resultList.clear();
        resultList.addAll(resultService.getAllResults());
        resultTable.setItems(resultList);
    }

    private void deleteResult(int id) {
        resultService.deleteResult(id);
        System.out.println("Résultat supprimé avec succès.");
    }

    // Method removed as functionality is now integrated in ListView cell factory

    @FXML
    void ajouterResult(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_result.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Résultat");
            stage.setScene(new Scene(loader.load()));

            AddResultController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadResultsFromDB());

            stage.show();
            stage.setOnHidden(e -> loadResultsFromDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exporterTable(ActionEvent event) {
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
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Results List", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Add space

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(7); // 7 columns
                pdfTable.setWidthPercentage(100);

                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"ID", "User ID", "Note", "Commentaire", "Réponses Correctes", "Réponses Incorrectes", "Quiz ID"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Result r : resultList) {
                    pdfTable.addCell(String.valueOf(r.getId()));
                    pdfTable.addCell(String.valueOf(r.getUserId()));
                    pdfTable.addCell(String.valueOf(r.getNote()));
                    pdfTable.addCell(r.getCommentaire());
                    pdfTable.addCell(String.valueOf(r.getNbRepCorrect()));
                    pdfTable.addCell(String.valueOf(r.getNbRepIncorrect()));
                    pdfTable.addCell(r.getQuizId() != null ? String.valueOf(r.getQuizId()) : "N/A");
                }

                document.add(pdfTable);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Result data has been exported to PDF successfully!");
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
    void navigateToQuiz(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("quizpage.fxml"));
            Scene scene = new Scene(loader.load());

            Stage newStage = new Stage();
            newStage.setTitle("Quiz");
            newStage.setScene(scene);
            newStage.show();

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de naviguer vers la page Quiz: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
