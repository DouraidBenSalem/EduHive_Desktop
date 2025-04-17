package Controllers;

import Entities.Classe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
import Services.ClasseService;
import Services.ClasseServiceImpl;

public class ClasseController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private TableColumn<Classe, Integer> id_classe;

    @FXML
    private TableColumn<Classe, String> classename;

    @FXML
    private TableColumn<Classe, Integer> num_etudiant;

    @FXML
    private TableView<Classe> classetable;

    @FXML
    private TableColumn<Classe, Void> editColumn;

    @FXML
    private TableColumn<Classe, Void> deleteColumn;

    private ObservableList<Classe> classeList = FXCollections.observableArrayList();
    private Connection connection;
    

    private ClasseService classeService = new ClasseServiceImpl();

    @FXML
    void initialize() {
        connection = MyDatabase.getInstance().getConnection();
        navbarController.setParent(this);
        id_classe.setCellValueFactory(new PropertyValueFactory<>("id"));
        classename.setCellValueFactory(new PropertyValueFactory<>("classename"));
        num_etudiant.setCellValueFactory(new PropertyValueFactory<>("num_etudiant"));

        loadClassesFromDB();
        addActionButtonsToTable();
    }

    private void loadClassesFromDB() {
        classeList.clear();
     
        classeList.addAll(classeService.getAllClasses());
        classetable.setItems(classeList);
    }

    private void deleteClasse(int id) {
    
        classeService.deleteClasse(id);
        System.out.println("Classe supprimée avec succès.");
    }

    private void addActionButtonsToTable() {
        // Configure edit column
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");

            {
                btnEdit.setOnAction(event -> {
                    Classe selectedClasse = getTableView().getItems().get(getIndex());
                    showEditDialog(selectedClasse);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEdit);
                }
            }
        });

        // Configure delete column
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("Delete");

            {
                btnDelete.setOnAction(event -> {
                    Classe selectedClasse = getTableView().getItems().get(getIndex());
                    deleteClasse(selectedClasse.getId());
                    loadClassesFromDB();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDelete);
                }
            }
        });
    }

    private void showEditDialog(Classe classe) {
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
            showErrorAlert("Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
        }
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
                String[] headers = {"ID", "Nom de classe", "Nombre d'étudiants"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Classe c : classeList) {
                    pdfTable.addCell(String.valueOf(c.getId()));
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


    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

   
    public void setParent(ClasseController parent) {
        
    }
}
