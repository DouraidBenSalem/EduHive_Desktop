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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
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

    @FXML
    private TextField searchField;

    @FXML
    private Button btnClearSearch;

    @FXML
    private Button btnExportMenu;

    private ComboBox<String> filterComboBox;

    private ObservableList<Matiere> matiereList = FXCollections.observableArrayList();

    private FilteredList<Matiere> filteredMatiereList;

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

        // Initialisation de la recherche avancée
        filteredMatiereList = new FilteredList<>(matiereList, p -> true);
        matiereTable.setItems(filteredMatiereList);

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
            File file = fileChooser.showSaveDialog(matiereTable.getScene().getWindow());
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
                        if (job != null && job.showPrintDialog(matiereTable.getScene().getWindow())) {
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