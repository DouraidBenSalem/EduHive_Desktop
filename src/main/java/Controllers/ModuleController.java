package Controllers;

import Entities.Module;
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

public class ModuleController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private TableColumn<Module, Integer> id_module;

    @FXML
    private TableColumn<Module, String> nom_module;

    @FXML
    private TableColumn<Module, String> description_module;
    
    @FXML
    private TableColumn<Module, String> module_img;
    
    @FXML
    private TableColumn<Module, Double> moy_module;

    @FXML
    private TableView<Module> moduletable;

    @FXML
    private TableColumn<Module, Void> editColumn;

    @FXML
    private TableColumn<Module, Void> deleteColumn;

    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Connection connection;

    @FXML
    void initialize() {
        connection = MyDatabase.getInstance().getConnection();
        navbarController.setParent(this);
        id_module.setCellValueFactory(new PropertyValueFactory<>("id"));
        nom_module.setCellValueFactory(new PropertyValueFactory<>("nom_module"));
        description_module.setCellValueFactory(new PropertyValueFactory<>("description_module"));
        module_img.setCellValueFactory(new PropertyValueFactory<>("module_img"));
        moy_module.setCellValueFactory(new PropertyValueFactory<>("moy"));

        loadModulesFromDB();
        addActionButtonsToTable();
    }

    private void loadModulesFromDB() {
        moduleList.clear();
        try {
            String query = "SELECT * FROM module";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Module m = new Module(
                        rs.getInt("id"),
                        rs.getString("nom_module"),
                        rs.getString("description_module"),
                        rs.getString("module_img"),
                        rs.getDouble("moy")
                );
                moduleList.add(m);
            }
            moduletable.setItems(moduleList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteModule(int id) {
        try {
            String query = "DELETE FROM module WHERE id = " + id;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            System.out.println("Module supprimé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtonsToTable() {
        // Configure edit column
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");

            {
                btnEdit.setOnAction(event -> {
                    Module selectedModule = getTableView().getItems().get(getIndex());
                    showEditDialog(selectedModule);
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
                    Module selectedModule = getTableView().getItems().get(getIndex());
                    deleteModule(selectedModule.getId());
                    loadModulesFromDB();
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

    private void showEditDialog(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajoutermodule.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier un Module");
            stage.setScene(new Scene(loader.load()));

            // Get the controller after loading
            add_module_controller controller = loader.getController();
            controller.initData(module);
            controller.setOnSaveCallback(() -> loadModulesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadModulesFromDB());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    void ajoutertable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajoutermodule.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Module");
            stage.setScene(new Scene(loader.load()));

            // Get the controller after loading
            add_module_controller controller = loader.getController();
            controller.setOnSaveCallback(() -> loadModulesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadModulesFromDB());
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
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Modules List", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Add space

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(5); // 5 columns
                pdfTable.setWidthPercentage(100);

                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                String[] headers = {"ID", "Nom du module", "Description", "Image", "Moyenne"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Module m : moduleList) {
                    pdfTable.addCell(String.valueOf(m.getId()));
                    pdfTable.addCell(m.getNom_module());
                    pdfTable.addCell(m.getDescription_module());
                    pdfTable.addCell(m.getModule_img() != null ? m.getModule_img() : "");
                    pdfTable.addCell(String.valueOf(m.getMoy()));
                }

                document.add(pdfTable);
                document.close();

                showInfoAlert("Export Successful", "Modules data has been exported to PDF successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("An error occurred while exporting to PDF: " + e.getMessage());
        }
    }

    @FXML
    void refrechtable(ActionEvent event) {
        loadModulesFromDB();
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

    // Methods for NavBarController
    public void setParent(ModuleController parent) {
        // This method is needed for NavBarController compatibility
    }
}