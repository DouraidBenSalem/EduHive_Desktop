package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import Entities.Cours;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import Services.CoursService;
import Services.CoursServiceImpl;
import java.util.List;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import java.awt.Desktop;
import java.io.File;
import javafx.scene.control.Hyperlink;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;
import java.io.FileWriter;
import java.io.PrintWriter;
import javafx.print.PrinterJob;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;

public class CoursListController {
    @FXML
    private TableView<Cours> tableCours;
    @FXML
    private TableColumn<Cours, Integer> colId;
    @FXML
    private TableColumn<Cours, String> colNom;
    @FXML
    private TableColumn<Cours, String> colDescription;
    @FXML
    private TableColumn<Cours, Integer> colOrdre;
    @FXML
    private TableColumn<Cours, String> colStatus;
    @FXML
    private TableColumn<Cours, String> colNiveau;
    @FXML
    private TableColumn<Cours, String> colPdf;
    // @FXML private TableColumn<Cours, String> colImage;
    @FXML
    private TableColumn<Cours, Integer> colMatiereId;
    @FXML
    private TableColumn<Cours, Integer> colPrerequis;
    @FXML
    private TableColumn<Cours, Void> colActions;
    @FXML
    private Button btnAddCours;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnClearSearch;
    @FXML
    private Button btnExportMenu;
    @FXML
    private ComboBox<String> filterComboBox;

    private final CoursService coursService = new CoursServiceImpl();
    private final ObservableList<Cours> coursList = FXCollections.observableArrayList();
    private FilteredList<Cours> filteredCoursList;

    @FXML
    public void initialize() {
        // Liaison des colonnes avec les propriétés de l'entité Cours
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCours"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCours"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusCours"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colPdf.setCellValueFactory(new PropertyValueFactory<>("pdfCours"));
        // colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        colMatiereId.setCellValueFactory(new PropertyValueFactory<>("matiereId"));
        colPrerequis.setCellValueFactory(new PropertyValueFactory<>("prerequisCoursId"));
        // ...vous pouvez ajouter la gestion des actions ici...

        loadCours();
        addActionsToTable();
        setPdfColumnWithButton();
        setDescriptionColumnWithModalButton();

        btnAddCours.setOnAction(e -> openAddCoursWindow());

        // Initialisation de la recherche avancée
        filteredCoursList = new FilteredList<>(coursList, p -> true);
        tableCours.setItems(filteredCoursList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredCoursList.setPredicate(cours -> {
                if (lower.isEmpty()) return true;
                return (cours.getNomCours() != null && cours.getNomCours().toLowerCase().contains(lower))
                    || (cours.getDescriptionCours() != null && cours.getDescriptionCours().toLowerCase().contains(lower))
                    || (cours.getStatusCours() != null && cours.getStatusCours().toLowerCase().contains(lower))
                    || (cours.getNiveau() != null && cours.getNiveau().toLowerCase().contains(lower))
                    || (cours.getPdfCours() != null && cours.getPdfCours().toLowerCase().contains(lower))
                    || (String.valueOf(cours.getMatiereId()).contains(lower))
                    || (cours.getPrerequisCoursId() != null && String.valueOf(cours.getPrerequisCoursId()).contains(lower));
            });
        });

        btnClearSearch.setOnAction(e -> searchField.clear());
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) searchField.clear();
        });

        setupExportMenu();
        setupFilterComboBox();
    }

    private void loadCours() {
        coursList.clear();
        List<Cours> all = coursService.getAllCours();
        if (all != null) {
            coursList.addAll(all);
        }
        // filteredCoursList est automatiquement mis à jour
    }

    private void openAddCoursWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_cours.fxml"));
            Parent root = loader.load();
            AddCoursController controller = loader.getController();
            controller.setOnSaveCallback(this::loadCours);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un cours");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addActionsToTable() {
        Callback<TableColumn<Cours, Void>, TableCell<Cours, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Cours, Void> call(final TableColumn<Cours, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");
                    private final HBox pane = new HBox(8, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #64b5f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6;");
                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6;");
                        pane.setPadding(new Insets(2, 0, 2, 0));

                        btnEdit.setOnAction(event -> {
                            Cours cours = getTableView().getItems().get(getIndex());
                            openEditCoursWindow(cours);
                        });

                        btnDelete.setOnAction(event -> {
                            Cours cours = getTableView().getItems().get(getIndex());
                            deleteCours(cours);
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
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void openEditCoursWindow(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/add_cours.fxml"));
            Parent root = loader.load();
            AddCoursController controller = loader.getController();
            controller.initData(cours);
            controller.setOnSaveCallback(this::loadCours);

            Stage stage = new Stage();
            stage.setTitle("Modifier le cours");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteCours(Cours cours) {
        if (cours != null) {
            coursService.deleteCours(cours.getId());
            loadCours();
        }
    }

    private void setPdfColumnWithButton() {
        colPdf.setCellFactory(col -> new TableCell<Cours, String>() {
            private final Button btnViewPdf = new Button("Voir PDF");
            {
                btnViewPdf.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6;");
                btnViewPdf.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    openPdfFile(cours.getPdfCours());
                });
            }
            @Override
            protected void updateItem(String pdfFileName, boolean empty) {
                super.updateItem(pdfFileName, empty);
                if (empty || pdfFileName == null || pdfFileName.isEmpty()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnViewPdf);
                }
            }
        });
    }

    private void openPdfFile(String pdfFileName) {
        if (pdfFileName == null || pdfFileName.isEmpty()) {
            return;
        }
        try {
            // Ouvre le PDF depuis le dossier pdfs/
            File pdfFile = new File("pdfs/" + pdfFileName);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } else {
                showError("Fichier PDF introuvable : " + pdfFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Impossible d'ouvrir le PDF : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void setDescriptionColumnWithModalButton() {
        colDescription.setCellFactory(col -> new TableCell<Cours, String>() {
            private final Button btnView = new Button("Voir");
            {
                btnView.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6;");
                btnView.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    showDescriptionModal(cours.getNomCours(), cours.getDescriptionCours());
                });
            }
            @Override
            protected void updateItem(String html, boolean empty) {
                super.updateItem(html, empty);
                if (empty || html == null || html.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnView);
                }
            }
        });
    }

    private void showDescriptionModal(String titre, String html) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Description du cours");

        // Titre stylé
        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #2196f3; -fx-padding: 0 0 12 0;");

        // WebView pour le contenu HTML
        WebView webView = new WebView();
        webView.setPrefSize(600, 350);
        webView.getEngine().loadContent(
            "<html><body style='font-family:Segoe UI;font-size:15px;padding:18px;background:#f7fbff;color:#222;'>" +
            html +
            "</body></html>"
        );

        // Bouton fermer
        Button btnClose = new Button("Fermer");
        btnClose.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 8;");
        btnClose.setOnAction(e -> modal.close());

        VBox vbox = new VBox(0, titleLabel, webView, btnClose);
        vbox.setSpacing(18);
        vbox.setStyle("-fx-background-color: white; -fx-padding: 24 24 24 24; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #b0d0ff, 16, 0.15, 0, 2);");
        vbox.setPrefSize(640, 480);
        vbox.setMinSize(400, 300);

        VBox.setMargin(btnClose, new Insets(10, 0, 0, 0));
        VBox.setMargin(titleLabel, new Insets(0, 0, 0, 0));

        Scene scene = new Scene(vbox);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void exportCoursToCsv() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter la liste des cours");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("cours_export.csv");
            File file = fileChooser.showSaveDialog(tableCours.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                    // En-tête CSV (sans colonnes ID)
                    writer.println("Nom;Description;Ordre;Status;Niveau;PDF");
                    for (Cours cours : filteredCoursList) {
                        String desc = cours.getDescriptionCours() != null
                            ? cours.getDescriptionCours().replaceAll("\\<.*?\\>", "").replaceAll("[\\r\\n]+", " ").replace(";", ",")
                            : "";
                        writer.printf("\"%s\";\"%s\";%d;\"%s\";\"%s\";\"%s\"%n",
                            safeCsv(cours.getNomCours()),
                            safeCsv(desc),
                            cours.getOrdre(),
                            safeCsv(cours.getStatusCours()),
                            safeCsv(cours.getNiveau()),
                            safeCsv(cours.getPdfCours())
                        );
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

    private void exportCoursToPdf() {
        try {
            // HTML tableau stylé, colonnes larges, police lisible
            StringBuilder html = new StringBuilder();
            html.append("<html><head><style>");
            html.append("body{font-family:'Segoe UI',Arial,sans-serif;background:#fff;color:#222;font-size:13px;}");
            html.append("table{border-collapse:collapse;width:100%;margin-top:10px;table-layout:fixed;}");
            html.append("th,td{border:1px solid #b0d0ff;padding:10px 8px;text-align:left;word-break:break-word;}");
            html.append("th{background:#e3f0ff;color:#2196f3;font-size:15px;}");
            html.append("tr:nth-child(even){background:#f7fbff;}");
            html.append("</style></head><body>");
            html.append("<h2 style='color:#2196f3;'>Liste des cours - EduHive</h2>");
            html.append("<table>");
            html.append("<colgroup>");
            html.append("<col style='width:18%;'/>"); // Nom
            html.append("<col style='width:36%;'/>"); // Description
            html.append("<col style='width:8%;'/>");  // Ordre
            html.append("<col style='width:12%;'/>"); // Status
            html.append("<col style='width:12%;'/>"); // Niveau
            html.append("<col style='width:14%;'/>"); // PDF
            html.append("</colgroup>");
            html.append("<tr>");
            html.append("<th>Nom</th>");
            html.append("<th>Description</th>");
            html.append("<th>Ordre</th>");
            html.append("<th>Status</th>");
            html.append("<th>Niveau</th>");
            html.append("<th>PDF</th>");
            html.append("</tr>");
            for (Cours cours : filteredCoursList) {
                String desc = cours.getDescriptionCours() != null
                    ? cours.getDescriptionCours().replaceAll("\\<.*?\\>", "").replaceAll("[\\r\\n]+", " ").replace("|", "/")
                    : "";
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(cours.getNomCours())).append("</td>");
                html.append("<td>").append(escapeHtml(desc)).append("</td>");
                html.append("<td>").append(cours.getOrdre()).append("</td>");
                html.append("<td>").append(escapeHtml(cours.getStatusCours())).append("</td>");
                html.append("<td>").append(escapeHtml(cours.getNiveau())).append("</td>");
                html.append("<td>").append(escapeHtml(cours.getPdfCours())).append("</td>");
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
                        if (job != null && job.showPrintDialog(tableCours.getScene().getWindow())) {
                            // Ajuste la hauteur pour tout imprimer
                            Object scrollHeight = webView.getEngine().executeScript("document.body.scrollHeight");
                            if (scrollHeight instanceof Number) {
                                webView.setPrefHeight(((Number) scrollHeight).doubleValue() + 40);
                            }
                            boolean success = job.printPage(webView);
                            if (success) {
                                job.endJob();
                                showAlert("Export PDF réussi !\nVous pouvez enregistrer le PDF via l'imprimante PDF de votre système.");
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
        if (value == null) return "";
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

    private void setupExportMenu() {
        ContextMenu exportMenu = new ContextMenu();

        MenuItem exportPdf = new MenuItem("Exporter en PDF");
        MenuItem exportExcel = new MenuItem("Exporter en Excel (CSV)");

        exportPdf.setOnAction(e -> exportCoursToPdf());
        exportExcel.setOnAction(e -> exportCoursToCsv());

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
            "Ordre",
            "Status",
            "Niveau",
            "PDF",
            "Matière ID",
            "Prérequis"
        ));
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
            "-fx-text-fill: #2196f3;"
        );
        filterComboBox.setPromptText("Filtrer par...");

        // Ajout dynamique du ComboBox dans la barre de recherche
        HBox searchBar = (HBox) searchField.getParent();
        searchBar.getChildren().add(1, filterComboBox);

        // Ajout d'un effet au survol
        filterComboBox.setOnMouseEntered(e -> filterComboBox.setStyle(
            filterComboBox.getStyle() + "-fx-border-color: #2196f3; -fx-background-color: #e3f0ff;"
        ));
        filterComboBox.setOnMouseExited(e -> filterComboBox.setStyle(
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-background-color: #f7fbff;" +
            "-fx-border-color: #b0d0ff;" +
            "-fx-border-width: 1.2;" +
            "-fx-font-size: 15px;" +
            "-fx-padding: 6 18 6 18;" +
            "-fx-pref-width: 160;" +
            "-fx-text-fill: #2196f3;"
        ));

        // Listener pour filtrer selon la colonne choisie
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            searchField.clear();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            String filter = filterComboBox.getValue();
            filteredCoursList.setPredicate(cours -> {
                if (lower.isEmpty() || filter == null || filter.equals("Tous")) {
                    // Recherche sur tous les champs
                    return (cours.getNomCours() != null && cours.getNomCours().toLowerCase().contains(lower))
                        || (cours.getDescriptionCours() != null && cours.getDescriptionCours().toLowerCase().contains(lower))
                        || (cours.getStatusCours() != null && cours.getStatusCours().toLowerCase().contains(lower))
                        || (cours.getNiveau() != null && cours.getNiveau().toLowerCase().contains(lower))
                        || (cours.getPdfCours() != null && cours.getPdfCours().toLowerCase().contains(lower))
                        || (String.valueOf(cours.getOrdre()).contains(lower))
                        || (String.valueOf(cours.getMatiereId()).contains(lower))
                        || (cours.getPrerequisCoursId() != null && String.valueOf(cours.getPrerequisCoursId()).contains(lower));
                } else {
                    switch (filter) {
                        case "Nom":
                            return cours.getNomCours() != null && cours.getNomCours().toLowerCase().contains(lower);
                        case "Description":
                            return cours.getDescriptionCours() != null && cours.getDescriptionCours().toLowerCase().contains(lower);
                        case "Ordre":
                            return String.valueOf(cours.getOrdre()).contains(lower);
                        case "Status":
                            return cours.getStatusCours() != null && cours.getStatusCours().toLowerCase().contains(lower);
                        case "Niveau":
                            return cours.getNiveau() != null && cours.getNiveau().toLowerCase().contains(lower);
                        case "PDF":
                            return cours.getPdfCours() != null && cours.getPdfCours().toLowerCase().contains(lower);
                        case "Matière ID":
                            return String.valueOf(cours.getMatiereId()).contains(lower);
                        case "Prérequis":
                            return cours.getPrerequisCoursId() != null && String.valueOf(cours.getPrerequisCoursId()).contains(lower);
                        default:
                            return true;
                    }
                }
            });
        });
    }
}
