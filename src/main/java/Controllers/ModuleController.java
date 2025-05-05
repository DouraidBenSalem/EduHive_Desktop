package Controllers;

import Entities.Module;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.MyDatabase;
import Services.ModuleService;
import Services.ModuleServiceImpl;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
<<<<<<< HEAD
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import Services.ModuleService;
import Services.ModuleServiceImpl;
=======
>>>>>>> wael

public class ModuleController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button exporter;

    @FXML
    private Button aiGeneratorBtn;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Module> moduletable;

    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Connection connection;
    

    private ModuleService moduleService = new ModuleServiceImpl();

    private ModuleService moduleService = new ModuleServiceImpl();

    @FXML
    void initialize() {
        connection = MyDatabase.getInstance().getConnection();
        navbarController.setParent(this);
        loadModulesFromDB();
        setupListView();
        setupSearch();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                moduletable.setItems(moduleList);
            } else {
                String searchTerm = newValue.toLowerCase();
                ObservableList<Module> filteredList = moduleList.filtered(module ->
                    module.getNom_module().toLowerCase().contains(searchTerm) ||
                    module.getDescription_module().toLowerCase().contains(searchTerm)
                );
                moduletable.setItems(filteredList);
            }
        });
    }

    private void loadModulesFromDB() {
        moduleList.clear();
<<<<<<< HEAD
      
=======
>>>>>>> wael
        moduleList.addAll(moduleService.getAllModules());
        moduletable.setItems(moduleList);
    }

    private void deleteModule(int id) {
<<<<<<< HEAD

        moduleService.deleteModule(id);
        System.out.println("Module supprimé avec succès.");
=======
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce module ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                moduleService.deleteModule(id);
                loadModulesFromDB();
                showInfoAlert("Succès", "Le module a été supprimé avec succès.");
            }
        });
>>>>>>> wael
    }

    private void setupListView() {
        moduletable.setCellFactory(param -> new ListCell<Module>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete);

            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label nameLabel = new Label("Module: " + item.getNom_module());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                    Label descLabel = new Label("Description: " + item.getDescription_module());
                    descLabel.setStyle("-fx-font-size: 13px;");

                    VBox content = new VBox(5, nameLabel, descLabel, buttons);
                    content.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5px;");

                    // Configure Edit button
                    btnEdit.setOnAction(event -> openEditForm(item));

                    // Configure Delete button
                    btnDelete.setOnAction(event -> {
                        deleteModule(item.getId());
                        loadModulesFromDB();
                    });

                    setText(null);
                    setGraphic(content);
                }
            }
        });
    }

    private void openEditForm(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajoutermodule.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier un Module");
            stage.setScene(new Scene(loader.load()));

<<<<<<< HEAD
    
=======
>>>>>>> wael
            add_module_controller controller = loader.getController();
            controller.initData(module);
            controller.setOnSaveCallback(() -> loadModulesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadModulesFromDB());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    @FXML
    void ajoutertable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ajoutermodule.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Module");
            stage.setScene(new Scene(loader.load()));

<<<<<<< HEAD
       
=======
>>>>>>> wael
            add_module_controller controller = loader.getController();
            controller.setOnSaveCallback(() -> loadModulesFromDB());

            stage.show();
            stage.setOnHidden(e -> loadModulesFromDB());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    @FXML
    void exportertable(ActionEvent event) {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save PDF File");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(exporter.getScene().getWindow());

            if (file != null) {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Modules List", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD));
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
<<<<<<< HEAD
                document.add(new com.itextpdf.text.Paragraph(" ")); 

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(5); 
=======
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(3);
>>>>>>> wael
                pdfTable.setWidthPercentage(100);

                String[] headers = {"Nom du module", "Description", "Image"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    pdfTable.addCell(cell);
                }

                for (Module m : moduleList) {
                    pdfTable.addCell(m.getNom_module());
                    pdfTable.addCell(m.getDescription_module());
                    pdfTable.addCell(m.getModule_img() != null ? m.getModule_img() : "");
                }

                document.add(pdfTable);
                document.close();

                showInfoAlert("Export réussi", "Les modules ont été exportés en PDF avec succès !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur d'export : " + e.getMessage());
        }
    }

<<<<<<< HEAD
    

=======
>>>>>>> wael
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

<<<<<<< HEAD

    public void setParent(ModuleController parent) {
       
=======
    public void setParent(ModuleController parent) {
        // Optional: for NavBarController communication
>>>>>>> wael
    }

    @FXML
    void openAIGenerator(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ai_module_generator.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Générateur de Module AI");
            stage.setScene(new Scene(loader.load()));

            // When the AI generator window is closed, refresh the module list
            stage.setOnHidden(e -> loadModulesFromDB());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du générateur AI : " + e.getMessage());
        }
    }
}
