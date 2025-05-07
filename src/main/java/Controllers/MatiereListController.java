package Controllers;

import Entities.Matiere;
import Entities.Module;
import Entities.User;
import Services.ModuleService;
import Services.ModuleServiceImpl;
import Services.UserService;
import Services.UserServiceImplementation;
import utils.MyDatabase;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MatiereListController implements Initializable {

    @FXML
    private GridPane matiereGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnAddMatiere;

    @FXML
    private Button btnClearSearch;

    @FXML
    private Button btnExportMenu;

    @FXML
    private ComboBox<String> filterComboBox;

    private MatiereService matiereService;
    private ModuleService moduleService = new ModuleServiceImpl();
    private UserService userService = new UserServiceImplementation(MyDatabase.getInstance().getConnection());
    private ObservableList<Matiere> matiereList;
    private FilteredList<Matiere> filteredMatieres;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matiereService = new MatiereServiceImpl();
        matiereGrid.getStyleClass().add("matiere-grid");
        searchField.getStyleClass().add("search-field");

        // Configuration du ComboBox de filtrage
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Nom",
                "Description",
                "Module",
                "Enseignant"));
        filterComboBox.setValue("Nom");

        loadMatieres();
        setupListeners();
        updateMatiereGrid();
    }

    private void loadMatieres() {
        // Charger les matières depuis le service
        matiereList = FXCollections.observableArrayList(matiereService.getAllMatieres());
        filteredMatieres = new FilteredList<>(matiereList, p -> true);
        updateMatiereGrid();
    }

    private void setupListeners() {
        // Configuration du bouton d'ajout
        btnAddMatiere.setOnAction(event -> openAddMatiereForm());

        // Configuration de la recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredMatieres.setPredicate(matiere -> {
                if (lower.isEmpty())
                    return true;

                Module module = moduleService.getModuleById(matiere.getModuleId());
                User enseignant = userService.getUserById(matiere.getEnseignantId());
                String moduleNom = module != null ? module.getNom_module().toLowerCase() : "";
                String enseignantNom = enseignant != null
                        ? (enseignant.getNom() + " " + enseignant.getPrenom()).toLowerCase()
                        : "";

                String filterCriteria = filterComboBox.getValue();
                switch (filterCriteria) {
                    case "Nom":
                        return matiere.getNomMatiere() != null && matiere.getNomMatiere().toLowerCase().contains(lower);
                    case "Description":
                        return matiere.getDescriptionMatiere() != null
                                && matiere.getDescriptionMatiere().toLowerCase().contains(lower);
                    case "Module":
                        return moduleNom.contains(lower);
                    case "Enseignant":
                        return enseignantNom.contains(lower);
                    default:
                        return false;
                }
            });
            updateMatiereGrid();
        });

        // Mise à jour du filtre lorsque le critère change
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!searchField.getText().isEmpty()) {
                // Déclencher la recherche avec le nouveau critère
                String currentText = searchField.getText();
                searchField.setText(currentText + " ");
                searchField.setText(currentText);
            }
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

    private void updateMatiereGrid() {
        matiereGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        int maxColumns = 2; // Nombre de colonnes dans la grille

        for (Matiere matiere : filteredMatieres) {
            VBox card = createMatiereCard(matiere);
            matiereGrid.add(card, column, row);

            column++;
            if (column == maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createMatiereCard(Matiere matiere) {
        VBox card = new VBox(10);
        card.getStyleClass().add("matiere-card");

        // Création du conteneur pour l'image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("matiere-image-container");

        // Création de l'ImageView
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("matiere-image");

        // Chargement de l'image avec gestion des erreurs
        try {
            String imageUrl = matiere.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = new Image(imageUrl, true);
                imageView.setImage(image);
            } else {
                // Image par défaut si aucune URL n'est disponible
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_matiere.png")));
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser l'image par défaut
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_matiere.png")));
            } catch (Exception ex) {
                // Si l'image par défaut ne peut pas être chargée, ne rien afficher
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            }
        }

        imageContainer.getChildren().add(imageView);

        // Titre de la matière
        Label titleLabel = new Label(matiere.getNomMatiere());
        titleLabel.getStyleClass().add("matiere-title");

        // Description de la matière
        Label descriptionLabel = new Label(
                matiere.getDescriptionMatiere() != null
                        ? (matiere.getDescriptionMatiere().length() > 100
                                ? matiere.getDescriptionMatiere().substring(0, 100) + "..."
                                : matiere.getDescriptionMatiere())
                        : "Aucune description");
        descriptionLabel.getStyleClass().add("matiere-description");

        // Détails de la matière
        Module module = moduleService.getModuleById(matiere.getModuleId());
        User enseignant = userService.getUserById(matiere.getEnseignantId());
        String moduleNom = module != null ? module.getNom_module() : "Module inconnu";
        String enseignantNom = enseignant != null ? enseignant.getNom() + " " + enseignant.getPrenom()
                : "Enseignant inconnu";
        Label detailsLabel = new Label(String.format("Module: %s | Enseignant: %s",
                moduleNom, enseignantNom));
        detailsLabel.getStyleClass().add("matiere-details");

        // Conteneur pour les boutons d'action
        HBox actionButtons = new HBox(10);
        actionButtons.getStyleClass().add("matiere-actions");

        // Boutons d'action
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("matiere-button", "matiere-button-edit");
        editButton.setOnAction(e -> editMatiere(matiere));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("matiere-button", "matiere-button-delete");
        deleteButton.setOnAction(e -> deleteMatiere(matiere.getId()));

        Button detailsButton = new Button("Détails");
        detailsButton.getStyleClass().addAll("matiere-button", "matiere-button-details");
        detailsButton.setOnAction(e -> showDescriptionModal(matiere));

        actionButtons.getChildren().addAll(editButton, deleteButton, detailsButton);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageContainer, titleLabel, descriptionLabel, detailsLabel, actionButtons);

        return card;
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

    private void deleteMatiere(int id) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette matière ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                matiereService.deleteMatiere(id);
                loadMatieres();
                showAlert("Succès", "La matière a été supprimée avec succès.");
            }
        });
    }

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}