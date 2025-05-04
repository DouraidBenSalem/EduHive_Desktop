package Controllers;

import Entities.Matiere;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserMatiereController implements Initializable {

    @FXML
    private GridPane matiereGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnClearSearch;

    @FXML
    private Pagination pagination;

    @FXML
    private Button btnRetourMain;

    private MatiereService matiereService;
    private ObservableList<Matiere> matiereList;
    private FilteredList<Matiere> filteredMatieres;

    // Constantes pour la pagination
    private static final int ITEMS_PER_PAGE = 6; // 2 lignes de 3 matières par page

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matiereService = new MatiereServiceImpl();
        matiereGrid.getStyleClass().add("matiere-grid-user");
        searchField.getStyleClass().add("search-field-user");
        loadMatieres();
        setupListeners();
        setupPagination();
    }

    private void loadMatieres() {
        // Charger les matières depuis le service
        matiereList = FXCollections.observableArrayList(matiereService.getAllMatieres());
        filteredMatieres = new FilteredList<>(matiereList, p -> true);
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) filteredMatieres.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount)); // Au moins 1 page même si vide
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5); // Nombre de boutons de page visibles

        // Mettre à jour la grille quand la page change
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            updateMatiereGrid();
        });

        updateMatiereGrid();
    }

    private void setupListeners() {
        // Configuration de la recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredMatieres.setPredicate(matiere -> {
                if (lower.isEmpty())
                    return true;
                return (matiere.getNomMatiere() != null && matiere.getNomMatiere().toLowerCase().contains(lower))
                        || (matiere.getDescriptionMatiere() != null
                                && matiere.getDescriptionMatiere().toLowerCase().contains(lower))
                        || (matiere.getObjectifMatiere() != null
                                && matiere.getObjectifMatiere().toLowerCase().contains(lower));
            });

            // Recalculer le nombre de pages après filtrage
            int pageCount = (int) Math.ceil((double) filteredMatieres.size() / ITEMS_PER_PAGE);
            pagination.setPageCount(Math.max(1, pageCount));
            pagination.setCurrentPageIndex(0); // Retour à la première page après recherche

            updateMatiereGrid();
        });

        // Configuration du bouton pour effacer la recherche
        btnClearSearch.setOnAction(event -> searchField.clear());
    }

    private void updateMatiereGrid() {
        matiereGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        int maxColumns = 3; // Nombre de colonnes dans la grille

        // Calculer l'index de début et de fin pour la pagination
        int currentPage = pagination.getCurrentPageIndex();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredMatieres.size());

        // Récupérer uniquement les matières de la page courante
        ObservableList<Matiere> currentPageItems = FXCollections.observableArrayList(
                filteredMatieres.stream()
                        .skip(startIndex)
                        .limit(ITEMS_PER_PAGE)
                        .collect(Collectors.toList()));

        for (Matiere matiere : currentPageItems) {
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
        card.getStyleClass().add("matiere-card-user");

        // Création du conteneur pour l'image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("matiere-image-container-user");

        // Création de l'ImageView
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("matiere-image-user");

        // Chargement de l'image avec gestion des erreurs
        try {
            String imageUrl = matiere.getImageUrl();
            System.out.println("Tentative de chargement de l'image: " + imageUrl); // Débogage

            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Vérifier si l'URL est relative ou absolue
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")
                        && !imageUrl.startsWith("file:")) {
                    // Si c'est un chemin relatif, essayer de le charger comme une ressource
                    try {
                        // Utiliser le constructeur qui accepte un InputStream
                        InputStream inputStream = getClass().getResourceAsStream(imageUrl);
                        Image image; // Déclarer la variable en dehors des blocs conditionnels
                        if (inputStream != null) {
                            image = new Image(inputStream);
                            imageView.setImage(image);
                        } else {
                            System.out.println("Ressource introuvable, tentative comme fichier local");
                            // Si la ressource n'est pas trouvée, essayer comme fichier local
                            image = new Image("file:" + imageUrl, true);
                            imageView.setImage(image);
                        }
                    } catch (Exception e) {
                        System.out.println("Exception lors du chargement comme ressource: " + e.getMessage());
                        // Essayer comme fichier local
                        Image image = new Image("file:" + imageUrl, true);
                        imageView.setImage(image);
                    }
                } else {
                    // Si c'est une URL absolue, la charger directement
                    Image image = new Image(imageUrl, true);
                    imageView.setImage(image);
                }

                // Ajouter un gestionnaire d'erreur pour l'image
                imageView.imageProperty().addListener((obs, oldImg, newImg) -> {
                    if (newImg != null && newImg.isError()) {
                        System.err.println("Erreur lors du chargement de l'image: " + imageUrl);
                        try {
                            // Utiliser une image par défaut du système
                            imageView.setImage(new Image(getClass().getResourceAsStream("/style_css/images/logo.png")));
                        } catch (Exception ex) {
                            System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
                            // Créer un rectangle gris comme placeholder
                            Rectangle placeholder = new Rectangle(280, 160, Color.LIGHTGRAY);
                            imageContainer.getChildren().clear();
                            imageContainer.getChildren().add(placeholder);
                        }
                    }
                });
            } else {
                // Image par défaut si aucune URL n'est disponible
                try {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/style_css/images/logo.png")));
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
                    // Créer un rectangle gris comme placeholder
                    Rectangle placeholder = new Rectangle(280, 160, Color.LIGHTGRAY);
                    imageContainer.getChildren().clear();
                    imageContainer.getChildren().add(placeholder);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception lors du chargement de l'image: " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, utiliser l'image par défaut
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/style_css/images/logo.png")));
            } catch (Exception ex) {
                // Si l'image par défaut ne peut pas être chargée, créer un placeholder
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
                Rectangle placeholder = new Rectangle(280, 160, Color.LIGHTGRAY);
                imageContainer.getChildren().clear();
                imageContainer.getChildren().add(placeholder);
            }
        }

        imageContainer.getChildren().add(imageView);

        // Titre de la matière
        Label titleLabel = new Label(matiere.getNomMatiere());
        titleLabel.getStyleClass().add("matiere-title-user");

        // Description de la matière
        Label descriptionLabel = new Label(
                matiere.getDescriptionMatiere() != null
                        ? (matiere.getDescriptionMatiere().length() > 100
                                ? matiere.getDescriptionMatiere().substring(0, 100) + "..."
                                : matiere.getDescriptionMatiere())
                        : "Aucune description");
        descriptionLabel.getStyleClass().add("matiere-description-user");

        // Objectif de la matière
        Label objectifLabel = new Label(
                matiere.getObjectifMatiere() != null
                        ? ("Objectif: " + (matiere.getObjectifMatiere().length() > 80
                                ? matiere.getObjectifMatiere().substring(0, 80) + "..."
                                : matiere.getObjectifMatiere()))
                        : "Aucun objectif défini");
        objectifLabel.getStyleClass().add("matiere-objectif-user");

        // Conteneur pour les boutons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        // Bouton pour voir les détails
        Button detailsButton = new Button("Voir détails");
        detailsButton.getStyleClass().add("matiere-button-details-user");
        detailsButton.setOnAction(e -> showDescriptionModal(matiere));

        // Bouton pour voir les cours
        Button coursButton = new Button("Voir les cours");
        coursButton.getStyleClass().add("matiere-button-cours-user");
        coursButton.setOnAction(e -> handleVoirCoursClick(matiere));

        // Ajouter les boutons au conteneur
        buttonContainer.getChildren().addAll(detailsButton, coursButton);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageContainer, titleLabel, descriptionLabel, objectifLabel, buttonContainer);

        return card;
    }

    private void handleVoirCoursClick(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/user_cours.fxml"));
            Parent root = loader.load();

            UserCoursController coursController = loader.getController();
            coursController.setMatiereId(matiere.getId());

            Scene scene = new Scene(root);
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la navigation vers les cours");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDescriptionModal(Matiere matiere) {
        // Create modal components
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);

        // Create modal content
        Label titleLabel = new Label(matiere.getNomMatiere());
        titleLabel.getStyleClass().add("modal-title-user");

        TextArea descriptionText = new TextArea(matiere.getDescriptionMatiere());
        descriptionText.setEditable(false);
        descriptionText.setWrapText(true);
        descriptionText.getStyleClass().add("modal-content-user");
        descriptionText.setPrefHeight(200);
        descriptionText.setPrefWidth(400);

        // Objectif section
        Label objectifTitle = new Label("Objectif du cours");
        objectifTitle.getStyleClass().add("modal-section-title-user");

        TextArea objectifText = new TextArea(
                matiere.getObjectifMatiere() != null ? matiere.getObjectifMatiere() : "Aucun objectif défini");
        objectifText.setEditable(false);
        objectifText.setWrapText(true);
        objectifText.getStyleClass().add("modal-content-user");
        objectifText.setPrefHeight(150);
        objectifText.setPrefWidth(400);

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("modal-close-button-user");
        closeButton.setOnAction(e -> modalStage.close());

        // Layout
        VBox modalContent = new VBox(15);
        modalContent.getStyleClass().add("modal-container-user");
        modalContent.getChildren().addAll(titleLabel, descriptionText, objectifTitle, objectifText, closeButton);
        modalContent.setAlignment(Pos.CENTER);

        // Scene
        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/style_css/user_matiere_style.css").toExternalForm());
        modalScene.setFill(null);

        // Stage
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    @FXML
    private void handleRetourMainClick() {
        try {
            // Charger la vue du menu principal (userPage.fxml semble être la page
            // principale)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
            Parent root = loader.load();

            // Afficher la vue du menu principal
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnRetourMain.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du retour au menu principal: " + e.getMessage());
        }
    }
}