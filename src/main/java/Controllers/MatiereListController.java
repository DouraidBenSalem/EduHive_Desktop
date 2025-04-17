package Controllers;

import Entities.Matiere;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MatiereListController implements Initializable {

    @FXML
    private ListView<Matiere> listMatiere;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnAddMatiere;

    @FXML
    private Button btnClearSearch;

    @FXML
    private Button btnExportMenu;

    private MatiereService matiereService;
    private ObservableList<Matiere> matiereList;
    private FilteredList<Matiere> filteredMatieres;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matiereService = new MatiereServiceImpl();
        loadMatieres();
        setupListeners();
        setupListView();
    }

    private void loadMatieres() {
        // Charger les matières depuis le service
        matiereList = FXCollections.observableArrayList(matiereService.getAllMatieres());
        filteredMatieres = new FilteredList<>(matiereList, p -> true);
        listMatiere.setItems(filteredMatieres);
    }

    private void setupListeners() {
        // Configuration du bouton d'ajout
        btnAddMatiere.setOnAction(event -> openAddMatiereForm());

        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMatieres.setPredicate(matiere -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (matiere.getNomMatiere().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        // Configuration du bouton pour effacer la recherche
        btnClearSearch.setOnAction(event -> searchField.clear());

        // Configuration du bouton d'export
        btnExportMenu.setOnAction(event -> {
            // Logique d'export à implémenter
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export");
            alert.setHeaderText(null);
            alert.setContentText("Fonctionnalité d'export à implémenter");
            alert.showAndWait();
        });
    }

    private void setupListView() {
        // Configuration de la cellule personnalisée pour le ListView
        listMatiere.setCellFactory(param -> new ListCell<Matiere>() {
            @Override
            protected void updateItem(Matiere matiere, boolean empty) {
                super.updateItem(matiere, empty);

                if (empty || matiere == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        // Créer une cellule personnalisée pour chaque matière
                        HBox cell = new HBox(10);
                        Label nomLabel = new Label(matiere.getNomMatiere());
                        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                        Button viewButton = new Button("Voir");
                        viewButton.setStyle("-fx-background-color: #58c7fa; -fx-text-fill: white;");
                        viewButton.setOnAction(event -> viewMatiere(matiere));

                        Button editButton = new Button("Modifier");
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        editButton.setOnAction(event -> editMatiere(matiere));

                        Button deleteButton = new Button("Supprimer");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        deleteButton.setOnAction(event -> deleteMatiere(matiere));

                        cell.getChildren().addAll(nomLabel, viewButton, editButton, deleteButton);
                        setGraphic(cell);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void openAddMatiereForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_matiere.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une matière");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Récupérer le contrôleur et lui passer les données nécessaires
            AddMatiereController controller = loader.getController();
            controller.setOnSaveCallback(this::loadMatieres);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout de matière.");
        }
    }

    private void viewMatiere(Matiere matiere) {
        // Afficher les détails de la matière
        showAlert("Détails de la matière", "Nom: " + matiere.getNomMatiere());
    }

    private void editMatiere(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_matiere.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Modifier une matière");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Récupérer le contrôleur et lui passer les données nécessaires
            AddMatiereController controller = loader.getController();
            controller.setMatiere(matiere);
            controller.setOnSaveCallback(this::loadMatieres);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification de matière.");
        }
    }

    private void deleteMatiere(Matiere matiere) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette matière ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                matiereService.deleteMatiere(matiere.getId());
                loadMatieres();
                showAlert("Succès", "La matière a été supprimée avec succès.");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}