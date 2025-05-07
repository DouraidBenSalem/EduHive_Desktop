package Controllers;

import Entities.Matiere;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.scene.web.WebView;
import javafx.print.PrinterJob;
import javafx.application.Platform;
import javafx.geometry.Insets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
    private ListView<Matiere> listMatiere;

    // Nous n'avons plus besoin des colonnes car nous utilisons ListView

    @FXML
    private TextField searchField;

    @FXML
    private Button btnClearSearch;

    @FXML
    private Button btnExportMenu;

    private ComboBox<String> filterComboBox;

    private ObservableList<Matiere> matiereList = FXCollections.observableArrayList();

    private FilteredList<Matiere> filteredMatiereList;

    @FXML
    public void navigateToStatistiques(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/statistiques_view.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    // Service for matiere operations
    private MatiereService matiereService = new MatiereServiceImpl();

    @FXML
    void initialize() {
        if (navbarController != null) {
            navbarController.setParent(this);
        }

        // Configuration du ListView
        listMatiere.setMinHeight(400);
        listMatiere.setPrefHeight(500);
        listMatiere.setMaxHeight(Double.MAX_VALUE); // Permet à la liste de s'étendre verticalement

        // Assurer que la liste peut défiler correctement
        VBox.setVgrow(listMatiere, javafx.scene.layout.Priority.ALWAYS);

        // Configuration des cellules personnalisées
        listMatiere.setCellFactory(lv -> new ListCell<Matiere>() {
            private final HBox container = new HBox(10);
            private final VBox infoContainer = new VBox(5);
            private final Label nomLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label detailsLabel = new Label();
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnViewDetails = new Button("Détails");
            private final HBox actionsContainer = new HBox(10, btnEdit, btnDelete, btnViewDetails);

            {
                // Style des éléments
                nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2196f3;");
                descriptionLabel.setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");
                detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");

                // Style des boutons
                btnEdit.setStyle(
                        "-fx-background-color: #58c7fa; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12 6 12;");
                btnDelete.setStyle(
                        "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12 6 12;");
                btnViewDetails.setStyle(
                        "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12 6 12;");

                // Organisation des conteneurs
                infoContainer.getChildren().addAll(nomLabel, descriptionLabel, detailsLabel);
                infoContainer.setPrefWidth(600);
                actionsContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                container.getChildren().addAll(infoContainer, actionsContainer);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setPadding(new Insets(10));
                container
                        .setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                // Limiter la largeur de la description
                descriptionLabel.setMaxWidth(580);
                descriptionLabel.setWrapText(true);
            }

            @Override
            protected void updateItem(Matiere matiere, boolean empty) {
                super.updateItem(matiere, empty);
                if (empty || matiere == null) {
                    setGraphic(null);
                } else {
                    // Mise à jour des informations
                    nomLabel.setText(matiere.getNomMatiere());

                    // Afficher une version tronquée de la description
                    String description = matiere.getDescriptionMatiere();
                    if (description != null && description.length() > 100) {
                        descriptionLabel.setText(description.substring(0, 100) + "...");
                    } else {
                        descriptionLabel.setText(description);
                    }

                    // Afficher uniquement l'objectif sans les IDs
                    detailsLabel.setText(String.format("Objectif: %s",
                            matiere.getObjectifMatiere() != null ? (matiere.getObjectifMatiere().length() > 50
                                    ? matiere.getObjectifMatiere().substring(0, 50) + "..."
                                    : matiere.getObjectifMatiere()) : "Non défini"));

                    // Configuration des actions des boutons
                    btnEdit.setOnAction(e -> editMatiere(matiere));
                    btnDelete.setOnAction(e -> deleteMatiere(matiere.getId()));
                    btnViewDetails.setOnAction(e -> showDescriptionModal(matiere));

                    setGraphic(container);
                }
            }
        });

        // Load data from database
        loadMatieresFromDB();

        // Initialisation de la recherche avancée
        filteredMatiereList = new FilteredList<>(matiereList, p -> true);
        listMatiere.setItems(filteredMatiereList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredMatiereList.setPredicate(matiere -> {
                if (lower.isEmpty())
                    return true;
                return (matiere.getNomMatiere() != null && matiere.getNomMatiere().toLowerCase().contains(lower))
                        || (matiere.getDescriptionMatiere() != null
                                && matiere.getDescriptionMatiere().toLowerCase().contains(lower))
                        || (matiere.getObjectifMatiere() != null
                                && matiere.getObjectifMatiere().toLowerCase().contains(lower))
                        || (String.valueOf(matiere.getModuleId()).contains(lower))
                        || (String.valueOf(matiere.getEnseignantId()).contains(lower))
                        || (matiere.getPrerequisMatiere() != null
                                && String.valueOf(matiere.getPrerequisMatiere()).contains(lower));
            });
        });

        btnClearSearch.setOnAction(e -> searchField.clear());
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                searchField.clear();
        });

        setupExportMenu();
        setupFilterComboBox();
    }

    private void loadMatieresFromDB() {
        matiereList.clear();
        // Use the service to get all matieres
        matiereList.addAll(matiereService.getAllMatieres());
        // filteredMatiereList est automatiquement mis à jour
    }

    private void editMatiere(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_matiere.fxml"));
            Scene scene = new Scene(loader.load());

            AddMatiereController controller = loader.getController();
            controller.initData(matiere);
            controller.setOnSaveCallback(() -> loadMatieresFromDB());

            Stage stage = new Stage();
            stage.setTitle("Modifier une Matière");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    void ajouterMatiere(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_matiere.fxml"));
            Scene scene = new Scene(loader.load());

            AddMatiereController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadMatieresFromDB());

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Matière");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
        }
    }

    private void setupExportMenu() {
        ContextMenu exportMenu = new ContextMenu();

        MenuItem exportPdf = new MenuItem("Exporter en PDF");
        MenuItem exportExcel = new MenuItem("Exporter en Excel (CSV)");

        exportPdf.setOnAction(e -> exportMatieresToPdf());
        exportExcel.setOnAction(e -> exportMatieresToCsv());

        exportMenu.getItems().addAll(exportPdf, exportExcel);

        // Affiche le menu uniquement au survol (hover) de l'icône
        btnExportMenu.setOnMouseEntered(e -> {
            if (!exportMenu.isShowing()) {
                exportMenu.show(btnExportMenu, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
        btnExportMenu.setOnMouseExited(e -> {
            // Cache le menu si la souris quitte le bouton ET le menu
            btnExportMenu.setOnMouseMoved(ev -> {
                if (!btnExportMenu.isHover() && !exportMenu.isShowing()) {
                    exportMenu.hide();
                }
            });
        });
        // Cache le menu si la souris quitte le menu contextuel
        exportMenu.setOnHidden(e -> btnExportMenu.disarm());
    }

    private void setupFilterComboBox() {
        // Ajout d'un ComboBox pour filtrer par colonne
        filterComboBox = new ComboBox<>();
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous",
                "Nom",
                "Description",
                "Objectif",
                "Module ID",
                "Enseignant ID",
                "Prérequis"));
        filterComboBox.setValue("Tous");

        // Style moderne et professionnel
        filterComboBox.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-color: #f7fbff;" +
                        "-fx-border-color: #b0d0ff;" +
                        "-fx-border-width: 1.2;" +
                        "-fx-font-size: 15px;" +
                        "-fx-padding: 6 18 6 18;" +
                        "-fx-pref-width: 160;" +
                        "-fx-text-fill: #58c7fa;");
        filterComboBox.setPromptText("Filtrer par...");

        // Ajout dynamique du ComboBox dans la barre de recherche
        HBox searchBar = (HBox) searchField.getParent();
        searchBar.getChildren().add(1, filterComboBox);

        // Ajout d'un effet au survol
        filterComboBox.setOnMouseEntered(e -> filterComboBox.setStyle(
                filterComboBox.getStyle() + "-fx-border-color: #58c7fa; -fx-background-color: #e3f0ff;"));
        filterComboBox.setOnMouseExited(e -> filterComboBox.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-color: #f7fbff;" +
                        "-fx-border-color: #b0d0ff;" +
                        "-fx-border-width: 1.2;" +
                        "-fx-font-size: 15px;" +
                        "-fx-padding: 6 18 6 18;" +
                        "-fx-pref-width: 160;" +
                        "-fx-text-fill: #58c7fa;"));

        // Listener pour filtrer selon la colonne choisie
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            searchField.clear();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            String filter = filterComboBox.getValue();
            filteredMatiereList.setPredicate(matiere -> {
                if (lower.isEmpty() || filter == null || filter.equals("Tous")) {
                    // Recherche sur tous les champs
                    return (matiere.getNomMatiere() != null && matiere.getNomMatiere().toLowerCase().contains(lower))
                            || (matiere.getDescriptionMatiere() != null
                                    && matiere.getDescriptionMatiere().toLowerCase().contains(lower))
                            || (matiere.getObjectifMatiere() != null
                                    && matiere.getObjectifMatiere().toLowerCase().contains(lower))
                            || (String.valueOf(matiere.getModuleId()).contains(lower))
                            || (String.valueOf(matiere.getEnseignantId()).contains(lower))
                            || (matiere.getPrerequisMatiere() != null
                                    && String.valueOf(matiere.getPrerequisMatiere()).contains(lower));
                } else {
                    switch (filter) {
                        case "Nom":
                            return matiere.getNomMatiere() != null
                                    && matiere.getNomMatiere().toLowerCase().contains(lower);
                        case "Description":
                            return matiere.getDescriptionMatiere() != null
                                    && matiere.getDescriptionMatiere().toLowerCase().contains(lower);
                        case "Objectif":
                            return matiere.getObjectifMatiere() != null
                                    && matiere.getObjectifMatiere().toLowerCase().contains(lower);
                        case "Module ID":
                            return String.valueOf(matiere.getModuleId()).contains(lower);
                        case "Enseignant ID":
                            return String.valueOf(matiere.getEnseignantId()).contains(lower);
                        case "Prérequis":
                            return matiere.getPrerequisMatiere() != null
                                    && String.valueOf(matiere.getPrerequisMatiere()).contains(lower);
                        default:
                            return true;
                    }
                }
            });
        });
    }

    private void exportMatieresToCsv() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter la liste des matières");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("matieres_export.csv");
            File file = fileChooser.showSaveDialog(listMatiere.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                    // En-tête CSV (sans colonnes ID)
                    writer.println("Nom;Description;Objectif;Module ID;Enseignant ID;Prérequis");
                    for (Matiere matiere : filteredMatiereList) {
                        String desc = matiere.getDescriptionMatiere() != null
                                ? matiere.getDescriptionMatiere().replaceAll("[\\r\\n]+", " ").replace(";", ",")
                                : "";
                        String objectif = matiere.getObjectifMatiere() != null
                                ? matiere.getObjectifMatiere().replaceAll("[\\r\\n]+", " ").replace(";", ",")
                                : "";
                        writer.printf("\"%s\";\"%s\";\"%s\";%d;%d;%d%n",
                                safeCsv(matiere.getNomMatiere()),
                                safeCsv(desc),
                                safeCsv(objectif),
                                matiere.getModuleId(),
                                matiere.getEnseignantId(),
                                matiere.getPrerequisMatiere() != null ? matiere.getPrerequisMatiere() : 0);
                    }
                }
                showAlert("Export Excel réussi !\nFichier : " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            showError("Erreur lors de l'export : " + ex.getMessage());
        }
    }

    private String safeCsv(String value) {
        return value == null ? "" : value.replace("\"", "\"\"");
    }

    private void exportMatieresToPdf() {
        try {
            // HTML tableau stylé, colonnes larges, police lisible
            StringBuilder html = new StringBuilder();
            html.append("<html><head><style>");
            html.append("body{font-family:'Segoe UI',Arial,sans-serif;background:#fff;color:#222;font-size:13px;}");
            html.append("table{border-collapse:collapse;width:100%;margin-top:10px;table-layout:fixed;}");
            html.append("th,td{border:1px solid #b0d0ff;padding:10px 8px;text-align:left;word-break:break-word;}");
            html.append("th{background:#e3f0ff;color:#58c7fa;font-size:15px;}");
            html.append("tr:nth-child(even){background:#f7fbff;}");
            html.append("</style></head><body>");
            html.append("<h2 style='color:#58c7fa;'>Liste des matières - EduHive</h2>");
            html.append("<table>");
            html.append("<colgroup>");
            html.append("<col style='width:18%;'/>"); // Nom
            html.append("<col style='width:25%;'/>"); // Description
            html.append("<col style='width:25%;'/>"); // Objectif
            html.append("<col style='width:10%;'/>"); // Module ID
            html.append("<col style='width:12%;'/>"); // Enseignant ID
            html.append("<col style='width:10%;'/>"); // Prérequis
            html.append("</colgroup>");
            html.append("<tr>");
            html.append("<th>Nom</th>");
            html.append("<th>Description</th>");
            html.append("<th>Objectif</th>");
            html.append("<th>Module ID</th>");
            html.append("<th>Enseignant ID</th>");
            html.append("<th>Prérequis</th>");
            html.append("</tr>");
            for (Matiere matiere : filteredMatiereList) {
                String desc = matiere.getDescriptionMatiere() != null
                        ? matiere.getDescriptionMatiere().replaceAll("[\\r\\n]+", " ").replace("|", "/")
                        : "";
                String objectif = matiere.getObjectifMatiere() != null
                        ? matiere.getObjectifMatiere().replaceAll("[\\r\\n]+", " ").replace("|", "/")
                        : "";
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(matiere.getNomMatiere())).append("</td>");
                html.append("<td>").append(escapeHtml(desc)).append("</td>");
                html.append("<td>").append(escapeHtml(objectif)).append("</td>");
                html.append("<td>").append(matiere.getModuleId()).append("</td>");
                html.append("<td>").append(matiere.getEnseignantId()).append("</td>");
                html.append("<td>").append(matiere.getPrerequisMatiere() != null ? matiere.getPrerequisMatiere() : "")
                        .append("</td>");
                html.append("</tr>");
            }
            html.append("</table></body></html>");

            WebView webView = new WebView();
            webView.setPrefWidth(1100);
            webView.setPrefHeight(900);
            webView.getEngine().loadContent(html.toString());

            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    Platform.runLater(() -> {
                        PrinterJob job = PrinterJob.createPrinterJob();
                        if (job != null && job.showPrintDialog(listMatiere.getScene().getWindow())) {
                            // Ajuste la hauteur pour tout imprimer
                            Object scrollHeight = webView.getEngine().executeScript("document.body.scrollHeight");
                            if (scrollHeight instanceof Number) {
                                webView.setPrefHeight(((Number) scrollHeight).doubleValue() + 40);
                            }
                            boolean success = job.printPage(webView);
                            if (success) {
                                job.endJob();
                                showAlert(
                                        "Export PDF réussi !\nVous pouvez enregistrer le PDF via l'imprimante PDF de votre système.");
                            }
                        }
                    });
                }
            });
        } catch (Exception ex) {
            showError("Erreur lors de l'export PDF : " + ex.getMessage());
        }
    }

    private String escapeHtml(String value) {
        if (value == null)
            return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void showAlert(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void deleteMatiere(int id) {
        // Use the service to delete a matiere
        matiereService.deleteMatiere(id);
        System.out.println("Matière supprimée avec succès.");
    }

    // Cette méthode n'est plus utilisée car nous avons migré vers ListView
    // Les actions sont maintenant gérées directement dans la cellule personnalisée

}