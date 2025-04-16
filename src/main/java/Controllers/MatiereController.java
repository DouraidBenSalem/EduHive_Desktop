package Controllers;

import Entities.Matiere;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import services.MatiereService;
import services.MatiereServiceImpl;

import java.io.IOException;

public class MatiereController {
    // Method to show description modal
    private void showDescriptionModal(Matiere matiere) {
        // Create modal components
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);

        // Create modal content
        Label titleLabel = new Label(matiere.getNomMatiere());
        titleLabel.getStyleClass().add("modal-title");

        TextArea descriptionText = new TextArea(matiere.getDescriptionMatiere());
        descriptionText.setEditable(false);
        descriptionText.setWrapText(true);
        descriptionText.getStyleClass().add("modal-content");
        descriptionText.setPrefHeight(200);
        descriptionText.setPrefWidth(400);

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());

        VBox modalContent = new VBox(10, titleLabel, descriptionText, closeButton);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.getStyleClass().add("modal-dialog");
        modalContent.setPrefWidth(450);

        // Create backdrop
        StackPane modalRoot = new StackPane(modalContent);
        modalRoot.getStyleClass().add("modal-backdrop");

        // Set scene and show modal
        Scene modalScene = new Scene(modalRoot, 500, 350);
        modalScene.setFill(null);
        modalScene.getStylesheets().add(getClass().getResource("/style_css/modal_style.css").toExternalForm());

        modalStage.setScene(modalScene);
        modalStage.show();
    }

    @FXML
    private NavBarController navbarController;

    @FXML
    private Button btnAjouter;

    @FXML
    private TableView<Matiere> matiereTable;

    @FXML
    private TableColumn<Matiere, Integer> idColumn;

    @FXML
    private TableColumn<Matiere, String> nomMatiereColumn;

    @FXML
    private TableColumn<Matiere, String> descriptionMatiereColumn;

    @FXML
    private TableColumn<Matiere, String> objectifMatiereColumn;

    @FXML
    private TableColumn<Matiere, Integer> moduleIdColumn;

    @FXML
    private TableColumn<Matiere, Integer> enseignantIdColumn;

    @FXML
    private TableColumn<Matiere, Integer> prerequisMatiereColumn;

    @FXML
    private TableColumn<Matiere, Void> actionColumn;

    private ObservableList<Matiere> matiereList = FXCollections.observableArrayList();

    // Service for matiere operations
    private MatiereService matiereService = new MatiereServiceImpl();

    @FXML
    void initialize() {
        if (navbarController != null) {
            navbarController.setParent(this);
        }

        // Set up the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomMatiereColumn.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        descriptionMatiereColumn.setCellValueFactory(new PropertyValueFactory<>("descriptionMatiere"));
        objectifMatiereColumn.setCellValueFactory(new PropertyValueFactory<>("objectifMatiere"));
        moduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("moduleId"));
        enseignantIdColumn.setCellValueFactory(new PropertyValueFactory<>("enseignantId"));
        prerequisMatiereColumn.setCellValueFactory(new PropertyValueFactory<>("prerequisMatiere"));

        // Configure description column to show modal on click
        descriptionMatiereColumn.setCellFactory(column -> {
            TableCell<Matiere, String> cell = new TableCell<Matiere, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Show truncated text in the cell
                        String displayText = item.length() > 30 ? item.substring(0, 30) + "..." : item;
                        setText(displayText);
                        getStyleClass().add("description-cell");
                    }
                }
            };

            // Add click event to show modal
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    Matiere matiere = cell.getTableView().getItems().get(cell.getIndex());
                    showDescriptionModal(matiere);
                }
            });

            return cell;
        });

        // Load data from database
        loadMatieresFromDB();

        // Set up action column if not already set
        if (actionColumn == null) {
            actionColumn = new TableColumn<>("Actions");
            matiereTable.getColumns().add(actionColumn);
        }

        // Add action buttons to the table
        addActionButtonsToTable();
    }

    private void loadMatieresFromDB() {
        matiereList.clear();
        // Use the service to get all matieres
        matiereList.addAll(matiereService.getAllMatieres());
        matiereTable.setItems(matiereList);
    }

    private void deleteMatiere(int id) {
        // Use the service to delete a matiere
        matiereService.deleteMatiere(id);
        System.out.println("Matière supprimée avec succès.");
    }

    private void addActionButtonsToTable() {
        // Définir une largeur suffisante pour la colonne d'action
        actionColumn.setPrefWidth(230);
        actionColumn.setMinWidth(230);

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                // Style pour le bouton Modifier
                btnEdit.setStyle(
                        "-fx-background-color: #58c7fa; -fx-text-fill: white; -fx-background-radius: 5px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEdit.setPrefWidth(100);

                // Style pour le bouton Supprimer
                btnDelete.setStyle(
                        "-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnDelete.setPrefWidth(100);

                // Assurer que le conteneur HBox a une largeur suffisante
                pane.setMinWidth(220);

                btnEdit.setOnAction(event -> {
                    Matiere selectedMatiere = getTableView().getItems().get(getIndex());

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_matiere.fxml"));
                        Scene scene = new Scene(loader.load());

                        AddMatiereController controller = loader.getController();
                        controller.initData(selectedMatiere);
                        controller.setOnSaveCallback(() -> loadMatieresFromDB());

                        Stage stage = new Stage();
                        stage.setTitle("Modifier une Matière");
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                btnDelete.setOnAction(event -> {
                    Matiere selectedMatiere = getTableView().getItems().get(getIndex());

                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Confirmation de suppression");
                    confirmDialog.setHeaderText("Supprimer la matière");
                    confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette matière?");

                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            deleteMatiere(selectedMatiere.getId());
                            loadMatieresFromDB();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    @FXML
    void ajouterMatiere(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_matiere.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Matière");
            stage.setScene(new Scene(loader.load()));

            AddMatiereController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadMatieresFromDB());

            stage.show();
            stage.setOnHidden(e -> loadMatieresFromDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}