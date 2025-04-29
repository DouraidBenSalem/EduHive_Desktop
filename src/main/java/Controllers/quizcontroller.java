package Controllers;

import Entities.quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.QuizService;
import services.QuizServiceImpl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.Comparator;

public class quizcontroller {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private Button Resultatpage;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private ListView<quiz> quizztable;

    private ObservableList<quiz> quizList = FXCollections.observableArrayList();
    private FilteredList<quiz> filteredList;

    // Add the service
    // Update the service instantiation
    private QuizService quizService = new QuizServiceImpl();

    @FXML
    void initialize() {
        navbarController.setParent(this);
        
        // Initialize sort options
        sortComboBox.getItems().addAll(
            "Titre (A-Z)",
            "Titre (Z-A)"
        );
        
        // Set default sort option
        sortComboBox.getSelectionModel().selectFirst();
        
        // Initialize search and sort functionality
        initializeSearchAndSort();
        
        loadQuizFromDB();

        // Configure ListView cell factory to display quiz information with card layout
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
                            // Create a card layout for the quiz
                            VBox cardLayout = new VBox(8);
                            cardLayout.setPadding(new javafx.geometry.Insets(10));
                            cardLayout.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
                            
                            // Title with styling
                            Label titleLabel = new Label(item.getTitre());
                            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3f51b5;");
                            
                            // Question with styling
                            Label questionLabel = new Label("Question: " + item.getQuestion());
                            questionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
                            questionLabel.setWrapText(true);
                            
                            // Correct answer with styling
                            Label answerLabel = new Label("Réponse correcte: " + item.getRepCorrect());
                            answerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #009688; -fx-font-style: italic;");
                            
                            // Options section
                            HBox optionsBox = new HBox(15);
                            optionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            
                            Label optionALabel = new Label("Option A: " + item.getOptionA());
                            optionALabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                            
                            Label optionBLabel = new Label("Option B: " + item.getOptionB());
                            optionBLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                            
                            optionsBox.getChildren().addAll(optionALabel, optionBLabel);
                            
                            // Style the buttons
                            btnEdit.getStyleClass().add("table-edit-button");
                            btnDelete.getStyleClass().add("table-delete-button");
                            buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                            
                            // Add all elements to the card
                            cardLayout.getChildren().addAll(titleLabel, questionLabel, answerLabel, optionsBox, buttons);
                            
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

                            setText(null); // Clear text as we're using a custom layout
                            setGraphic(cardLayout);
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
        filteredList = new FilteredList<>(quizList, p -> true);
        
        // Configure search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(quiz -> {
                // If search field is empty, show all quizzes
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against multiple fields
                if (quiz.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches title
                } else if (quiz.getQuestion().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches question
                } else if (quiz.getRepCorrect().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches correct answer
                } else if (quiz.getOptionA().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches option A
                } else if (quiz.getOptionB().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches option B
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
        SortedList<quiz> sortedList = new SortedList<>(filteredList);
        
        // Apply sort based on selected option
        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption != null) {
            switch (sortOption) {
                case "Titre (A-Z)":
                    sortedList.setComparator(Comparator.comparing(quiz::getTitre));
                    break;
                case "Titre (Z-A)":
                    sortedList.setComparator(Comparator.comparing(quiz::getTitre).reversed());
                    break;
                default:
                    sortedList.setComparator(null);
                    break;
            }
        }
        
        // Update ListView with sorted and filtered items
        quizztable.setItems(sortedList);
    }

    private void deleteQuiz(int id) {
        // Use the service instead of direct database access
        quizService.deleteQuiz(id);
        System.out.println("Quiz supprimé avec succès.");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    
    @FXML
    void prendreQuiz(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/take_quiz.fxml"));
            Scene scene = new Scene(loader.load());

            TakeQuizController controller = loader.getController();
            // Charger tous les quiz disponibles au lieu d'un quiz spécifique
            controller.loadAllQuizzes();

            Stage stage = new Stage();
            stage.setTitle("Tous les Quiz");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page des quiz.");
        }
    }


}
