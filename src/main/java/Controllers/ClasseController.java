package Controllers;

import Entities.Classe;
import Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.MyDatabase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import Services.ClasseService;
import Services.ClasseServiceImpl;
import Services.UserService;
import Services.UserServiceImplementation;
import Services.ResultService;
import Services.ResultServiceImpl;

public class ClasseController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private TextField searchField;

    @FXML
    private Button statisticsBtn;

    @FXML
    private Button equilibrageBtn;

    @FXML
    private TableColumn<Classe, Integer> id_classe;

    @FXML
    private TableColumn<Classe, String> classename;

    @FXML
    private TableColumn<Classe, Integer> num_etudiant;

    @FXML
    private ListView<Classe> classetable;

    @FXML
    private TableColumn<Classe, Void> editColumn;

    @FXML
    private TableColumn<Classe, Void> deleteColumn;

    private ObservableList<Classe> classeList = FXCollections.observableArrayList();
    private Connection connection;
    private ClasseService classeService = new ClasseServiceImpl();
    private UserService userService = new UserServiceImplementation();
    private ResultService resultService = new ResultServiceImpl();

    @FXML
    void initialize() {
        connection = MyDatabase.getInstance().getConnection();
        navbarController.setParent(this);
        loadClassesFromDB();
        setupListView();
        setupSearch();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                classetable.setItems(classeList);
            } else {
                String searchTerm = newValue.toLowerCase();
                ObservableList<Classe> filteredList = classeList.filtered(classe ->
                    classe.getClassename().toLowerCase().contains(searchTerm)
                );
                classetable.setItems(filteredList);
            }
        });
    }

    private void loadClassesFromDB() {
        classeList.clear();
        classeList.addAll(classeService.getAllClasses());
        // Load users for each class
        for (Classe classe : classeList) {
            List<User> users = userService.getUsersByClassId(classe.getId());
            classe.setUsers(users);
            
            // Calculate and update class average
            List<Integer> studentIds = classe.getStudents().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            double classAverage = resultService.calculateClassAverage(studentIds);
            classe.setClassemoy(classAverage);
            classeService.updateClasseAverage(classe.getId(), classAverage);
        }
        classetable.setItems(classeList);
    }

    private void deleteClasse(int id) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette classe ? Les étudiants et enseignants seront désassociés de cette classe.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    classeService.deleteClasse(id);
                    loadClassesFromDB();
                    showInfoAlert("Succès", "La classe a été supprimée avec succès.");
                } catch (RuntimeException e) {
                    showErrorAlert("Erreur lors de la suppression de la classe: " + e.getMessage());
                }
            }
        });
    }

    private void setupListView() {
        classetable.setCellFactory(param -> new ListCell<Classe>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete);
            
            @Override
            protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cardLayout = new VBox(8);
                    cardLayout.setPadding(new javafx.geometry.Insets(10));
                    cardLayout.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
                    
                    // Class name and student count
                    Label nameLabel = new Label("Classe: " + item.getClassename());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3f51b5;");
                    Label etudiantLabel = new Label("Nombre d'étudiants: " + item.getNum_etudiant());
                    etudiantLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #009688; -fx-font-style: italic;");
                    
                    // Class average
                    Label classAverageLabel = new Label(String.format("Moyenne de la classe: %.2f", item.getClassemoy()));
                    classAverageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff5722; -fx-font-size: 14px;");
                    
                    // Students section
                    VBox studentsBox = new VBox(5);
                    Label studentsTitle = new Label("Étudiants:");
                    studentsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196f3;");
                    studentsBox.getChildren().add(studentsTitle);

                    // Add students with their averages
                    for (User student : item.getStudents()) {
                        double studentAverage = resultService.calculateStudentAverage(student.getId());
                        Label studentLabel = new Label(String.format("• %s %s (Moyenne: %.2f)", 
                            student.getNom(), student.getPrenom(), studentAverage));
                        studentLabel.setStyle("-fx-text-fill: #424242;");
                        studentsBox.getChildren().add(studentLabel);
                    }

                    // Teachers section
                    VBox teachersBox = new VBox(5);
                    Label teachersTitle = new Label("Enseignants:");
                    teachersTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #4caf50;");
                    teachersBox.getChildren().add(teachersTitle);
                    for (User teacher : item.getTeachers()) {
                        Label teacherLabel = new Label("• " + teacher.getNom() + " " + teacher.getPrenom());
                        teacherLabel.setStyle("-fx-text-fill: #424242;");
                        teachersBox.getChildren().add(teacherLabel);
                    }

                    // Configure Edit button
                    btnEdit.setOnAction(event -> openEditForm(item));
                    btnEdit.getStyleClass().add("table-edit-button");

                    // Configure Delete button
                    btnDelete.setOnAction(event -> deleteClasse(item.getId()));
                    btnDelete.getStyleClass().add("table-delete-button");
                    
                    buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    cardLayout.getChildren().addAll(
                        nameLabel, 
                        etudiantLabel, 
                        classAverageLabel, 
                        studentsBox, 
                        teachersBox, 
                        buttons
                    );
                    setGraphic(cardLayout);
                }
            }
        });
    }

    private void openEditForm(Classe classe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajouterclasse.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier une Classe");
            stage.setScene(new Scene(loader.load()));

            add_classe_controller controller = loader.getController();
            controller.initData(classe);
            controller.setOnSaveCallback(() -> loadClassesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadClassesFromDB());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void ajoutertable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajouterclasse.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Classe");
            stage.setScene(new Scene(loader.load()));

            add_classe_controller controller = loader.getController();
            controller.setOnSaveCallback(() -> loadClassesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadClassesFromDB());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
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
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Classes List", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Add space

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(3); // 3 columns
                pdfTable.setWidthPercentage(100);

                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"Nom de classe", "Nombre d'étudiants"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Classe c : classeList) {
                    pdfTable.addCell(c.getClassename());
                    pdfTable.addCell(String.valueOf(c.getNum_etudiant()));
                }

                document.add(pdfTable);
                document.close();

                showInfoAlert("Export Successful", "Classes data has been exported to PDF successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("An error occurred while exporting to PDF: " + e.getMessage());
        }
    }

    public void setParent(ClasseController parent) {

    }

    @FXML
    void showStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("classestat.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Statistiques des Classes");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture des statistiques : " + e.getMessage());
        }
    }

    @FXML
    void handleEquilibrage(ActionEvent event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation d'équilibrage");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Voulez-vous procéder à l'avancement automatique des élèves et à l'équilibrage des classes ? \n\n" +
                                  "- Les élèves avec une moyenne ≥ 9 passeront au niveau supérieur\n" +
                                  "- Les classes seront équilibrées avec un mélange d'élèves forts et moins forts\n" +
                                  "- Les élèves avec une moyenne < 9 resteront dans leur classe actuelle");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    classeService.advanceAndBalanceClasses();
                    loadClassesFromDB(); // Refresh the view
                    showInfoAlert("Succès", "L'avancement et l'équilibrage des classes ont été effectués avec succès.");
                } catch (Exception e) {
                    showErrorAlert("Une erreur est survenue lors de l'équilibrage des classes : " + e.getMessage());
                }
            }
        });
    }
}
