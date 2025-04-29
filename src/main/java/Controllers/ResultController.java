package Controllers;

import Entities.Result;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.Comparator;

public class ResultController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporterBtn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private ListView<Result> resultTable;

    private ObservableList<Result> resultList = FXCollections.observableArrayList();
    private FilteredList<Result> filteredList;

    private ResultService resultService = new ResultServiceImpl();

    @FXML
    void initialize() {
        if (navbarController != null) {

        }
        
        // Initialize sort options
        sortComboBox.getItems().addAll(
            "Note (Croissant)",
            "Note (Décroissant)",
            "Réponses Correctes (Croissant)",
            "Réponses Correctes (Décroissant)"
        );
        
        // Set default sort option
        sortComboBox.getSelectionModel().selectFirst();
        
        // Initialize search and sort functionality
        initializeSearchAndSort();

        loadResultsFromDB();

        // Configure ListView cell factory to display result information with card layout
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
                            // Create a card layout for the result
                            javafx.scene.layout.VBox cardLayout = new javafx.scene.layout.VBox(8);
                            cardLayout.setPadding(new javafx.geometry.Insets(10));
                            cardLayout.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
                            
                            // User and Quiz info with styling
                            Label userLabel = new Label("Utilisateur: " + (item.getUserName() != null ? item.getUserName() : "ID: " + item.getUserId()));
                            userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3f51b5;");
                            
                            Label quizLabel = new Label("Quiz: " + (item.getQuizTitle() != null ? item.getQuizTitle() : "ID: " + item.getQuizId()));
                            quizLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
                            quizLabel.setWrapText(true);
                            
                            // Score with styling
                            Label scoreLabel = new Label("Note: " + item.getNote() + "/20");
                            scoreLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #009688; -fx-font-style: italic;");
                            
                            // Answers section
                            HBox answersBox = new HBox(15);
                            answersBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            
                            Label correctLabel = new Label("Réponses correctes: " + item.getNbRepCorrect());
                            correctLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
                            
                            Label incorrectLabel = new Label("Réponses incorrectes: " + item.getNbRepIncorrect());
                            incorrectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
                            
                            answersBox.getChildren().addAll(correctLabel, incorrectLabel);
                            
                            // Comment section if available
                            Label commentLabel = null;
                            if (item.getCommentaire() != null && !item.getCommentaire().isEmpty()) {
                                commentLabel = new Label("Commentaire: " + item.getCommentaire());
                                commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-style: italic;");
                                commentLabel.setWrapText(true);
                            }
                            
                            // Style the buttons
                            btnEdit.getStyleClass().add("table-edit-button");
                            btnDelete.getStyleClass().add("table-delete-button");
                            buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                            
                            // Add all elements to the card
                            cardLayout.getChildren().addAll(userLabel, quizLabel, scoreLabel, answersBox);
                            if (commentLabel != null) {
                                cardLayout.getChildren().add(commentLabel);
                            }
                            cardLayout.getChildren().add(buttons);
                            
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

                            setText(null); // Clear text as we're using a custom layout
                            setGraphic(cardLayout);
                        }
                    }
                };
            }
        });
    }

    private void loadResultsFromDB() {
        resultList.clear();
        resultList.addAll(resultService.getAllResults());
        
        // Initialize filtered list if not already done
        if (filteredList == null) {
            initializeSearchAndSort();
        } else {
            // Trigger filter refresh
            searchField.setText(searchField.getText());
        }
    }
    
    private void initializeSearchAndSort() {
        // Initialize filtered list
        filteredList = new FilteredList<>(resultList, p -> true);
        
        // Configure search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(result -> {
                // If search field is empty, show all results
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against multiple fields
                if (result.getUserName() != null && result.getUserName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches user name
                } else if (String.valueOf(result.getUserId()).contains(lowerCaseFilter)) {
                    return true; // Filter matches user ID
                } else if (result.getQuizTitle() != null && result.getQuizTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches quiz title
                } else if (String.valueOf(result.getQuizId()).contains(lowerCaseFilter)) {
                    return true; // Filter matches quiz ID
                } else if (String.valueOf(result.getNote()).contains(lowerCaseFilter)) {
                    return true; // Filter matches note
                } else if (String.valueOf(result.getNbRepCorrect()).contains(lowerCaseFilter)) {
                    return true; // Filter matches correct answers
                } else if (String.valueOf(result.getNbRepIncorrect()).contains(lowerCaseFilter)) {
                    return true; // Filter matches incorrect answers
                } else if (result.getCommentaire() != null && result.getCommentaire().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches commentaire
                }
                return false; // Does not match
            });
            
            // Apply current sort after filtering
            applySorting();
        });
        
        // Configure sort functionality
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                applySorting();
            }
        });
        
        // Initial application of filtering and sorting
        applySorting();
    }
    
    private void applySorting() {
        SortedList<Result> sortedList = new SortedList<>(filteredList);
        
        // Apply sort based on selected option
        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption != null) {
            switch (sortOption) {
                case "Note (Croissant)":
                    sortedList.setComparator(Comparator.comparing(Result::getNote));
                    break;
                case "Note (Décroissant)":
                    sortedList.setComparator(Comparator.comparing(Result::getNote).reversed());
                    break;
                case "Réponses Correctes (Croissant)":
                    sortedList.setComparator(Comparator.comparing(Result::getNbRepCorrect));
                    break;
                case "Réponses Correctes (Décroissant)":
                    sortedList.setComparator(Comparator.comparing(Result::getNbRepCorrect).reversed());
                    break;
                default:
                    sortedList.setComparator(null);
                    break;
            }
        }
        
        // Update ListView with sorted and filtered items
        resultTable.setItems(sortedList);
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


}
