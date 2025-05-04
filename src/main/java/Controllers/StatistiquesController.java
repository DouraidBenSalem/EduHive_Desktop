package Controllers;

import Entities.Cours;
import Entities.Matiere;
import Services.CoursService;
import Services.CoursServiceImpl;
import Services.MatiereService;
import Services.MatiereServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private VBox sideNavBar;

    private MatiereService matiereService;
    private CoursService coursService;

    // Enum pour les types de graphiques disponibles
    private enum ChartType {
        BAR_CHART("Graphique à barres"),
        PIE_CHART("Graphique circulaire"),
        AREA_CHART("Graphique sinusoïdal"),
        STACKED_BAR_CHART("Graphique à barres empilées"),
        BUBBLE_CHART("Graphique à bulles");

        private final String displayName;

        ChartType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Catégories de statistiques
    private enum StatCategory {
        COURS_PAR_MATIERE("Cours par Matière"),
        NIVEAUX_COURS("Niveaux des Cours"),
        STATUS_COURS("Statut des Cours"),
        DISTRIBUTION_MATIERES("Distribution des Matières");

        private final String displayName;

        StatCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Stockage du type de graphique actuel pour chaque catégorie
    private Map<StatCategory, ChartType> currentChartTypes = new HashMap<>();
    private StatCategory currentCategory = StatCategory.COURS_PAR_MATIERE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        matiereService = new MatiereServiceImpl();
        coursService = new CoursServiceImpl();

        // Initialisation des types de graphiques par défaut
        currentChartTypes.put(StatCategory.COURS_PAR_MATIERE, ChartType.BAR_CHART);
        currentChartTypes.put(StatCategory.NIVEAUX_COURS, ChartType.PIE_CHART);
        currentChartTypes.put(StatCategory.STATUS_COURS, ChartType.PIE_CHART);
        currentChartTypes.put(StatCategory.DISTRIBUTION_MATIERES, ChartType.AREA_CHART);

        // Configuration de la barre de navigation latérale
        setupSideNavBar();

        // Affichage du contenu initial
        updateMainContent();

        // Ajout d'un titre en haut
        Label titleLabel = new Label("Statistiques Éducatives");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2196f3"));
        titleLabel.setPadding(new Insets(15));

        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        mainContainer.setTop(titleBox);
    }

    /**
     * Configure la barre de navigation latérale avec les catégories et types de
     * graphiques
     */
    private void setupSideNavBar() {
        sideNavBar.getChildren().clear();

        // Titre de la barre latérale
        Label navTitle = new Label("Navigation");
        navTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        navTitle.setTextFill(Color.WHITE);
        navTitle.setPadding(new Insets(0, 0, 20, 0));
        sideNavBar.getChildren().add(navTitle);

        // Section des catégories
        Label categoriesLabel = new Label("CATÉGORIES");
        categoriesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        categoriesLabel.setTextFill(Color.web("#90caf9"));
        categoriesLabel.setPadding(new Insets(0, 0, 10, 0));
        sideNavBar.getChildren().add(categoriesLabel);

        // Boutons pour chaque catégorie
        for (StatCategory category : StatCategory.values()) {
            Button categoryBtn = createNavButton(category.getDisplayName(), e -> {
                currentCategory = category;
                updateMainContent();
            });

            // Mettre en évidence la catégorie active
            if (category == currentCategory) {
                categoryBtn.getStyleClass().add("active-nav-button");
            }

            sideNavBar.getChildren().add(categoryBtn);
        }

        // Séparateur
        Label separator = new Label("");
        separator.setPadding(new Insets(10, 0, 10, 0));
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setStyle("-fx-border-color: #90caf9; -fx-border-width: 0 0 1 0; -fx-opacity: 0.5;");
        sideNavBar.getChildren().add(separator);

        // Section des types de graphiques
        Label typesLabel = new Label("TYPES DE GRAPHIQUES");
        typesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        typesLabel.setTextFill(Color.web("#90caf9"));
        typesLabel.setPadding(new Insets(10, 0, 10, 0));
        sideNavBar.getChildren().add(typesLabel);

        // Boutons pour chaque type de graphique
        for (ChartType chartType : ChartType.values()) {
            Button typeBtn = createNavButton(chartType.getDisplayName(), e -> {
                currentChartTypes.put(currentCategory, chartType);
                updateMainContent();
            });

            // Mettre en évidence le type actif pour la catégorie courante
            if (chartType == currentChartTypes.get(currentCategory)) {
                typeBtn.getStyleClass().add("active-nav-button");
            }

            sideNavBar.getChildren().add(typeBtn);
        }
    }

    /**
     * Crée un bouton stylisé pour la barre de navigation
     */
    private Button createNavButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("nav-button");
        button.setOnAction(handler);
        return button;
    }

    /**
     * Met à jour le contenu principal en fonction de la catégorie et du type de
     * graphique sélectionnés
     */
    private void updateMainContent() {
        // Mise à jour de la barre de navigation pour refléter la sélection actuelle
        setupSideNavBar();

        // Création du contenu en fonction de la catégorie et du type de graphique
        Node content = null;
        ChartType currentChartType = currentChartTypes.get(currentCategory);

        switch (currentCategory) {
            case COURS_PAR_MATIERE:
                content = createCoursParMatiereChart(currentChartType);
                break;
            case NIVEAUX_COURS:
                content = createNiveauxCoursChart(currentChartType);
                break;
            case STATUS_COURS:
                content = createStatusCoursChart(currentChartType);
                break;
            case DISTRIBUTION_MATIERES:
                content = createDistributionMatieresChart(currentChartType);
                break;
        }

        // Ajout du contenu dans un ScrollPane pour gérer les grands graphiques
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("chart-scroll-pane");

        mainContainer.setCenter(scrollPane);
    }

    /**
     * Crée un graphique montrant le nombre de cours par matière selon le type de
     * graphique sélectionné
     */
    private VBox createCoursParMatiereChart(ChartType chartType) {
        // Récupération des données
        List<Matiere> matieres = matiereService.getAllMatieres();
        List<Cours> allCours = coursService.getAllCours();

        // Préparation des données pour le graphique
        Map<Integer, String> matiereNames = matieres.stream()
                .collect(Collectors.toMap(Matiere::getId, Matiere::getNomMatiere));

        Map<String, Integer> coursCountByMatiere = new HashMap<>();

        // Comptage des cours par matière
        for (Cours cours : allCours) {
            String matiereName = matiereNames.getOrDefault(cours.getMatiereId(), "Inconnu");
            coursCountByMatiere.put(matiereName, coursCountByMatiere.getOrDefault(matiereName, 0) + 1);
        }

        // Création du graphique selon le type sélectionné
        Chart chart = null;
        String title = "Nombre de Cours par Matière";

        switch (chartType) {
            case BAR_CHART:
                chart = createBarChart(coursCountByMatiere, title, "Matière", "Nombre de Cours");
                break;
            case PIE_CHART:
                chart = createPieChart(coursCountByMatiere, title);
                break;
            case AREA_CHART:
                chart = createAreaChart(coursCountByMatiere, title, "Matière", "Nombre de Cours");
                break;
            case STACKED_BAR_CHART:
                chart = createStackedBarChart(coursCountByMatiere, title, "Matière", "Nombre de Cours");
                break;
            case BUBBLE_CHART:
                chart = createBubbleChart(coursCountByMatiere, title, "Matière", "Nombre de Cours");
                break;
            default:
                chart = createBarChart(coursCountByMatiere, title, "Matière", "Nombre de Cours");
        }

        // Création d'un conteneur pour le graphique avec un titre descriptif
        Label descriptionLabel = new Label(
                "Ce graphique montre la répartition des cours par matière, permettant d'identifier les matières les plus développées.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(10));
        descriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

        VBox container = new VBox(10, chart, descriptionLabel);
        container.setPadding(new Insets(15));
        return container;
    }

    /**
     * Crée un graphique montrant la répartition des niveaux de cours selon le type
     * de graphique sélectionné
     */
    private VBox createNiveauxCoursChart(ChartType chartType) {
        // Récupération des données
        List<Cours> allCours = coursService.getAllCours();

        // Comptage des cours par niveau
        Map<String, Integer> coursCountByNiveau = new HashMap<>();
        for (Cours cours : allCours) {
            String niveau = cours.getNiveau() != null ? cours.getNiveau() : "Non défini";
            coursCountByNiveau.put(niveau, coursCountByNiveau.getOrDefault(niveau, 0) + 1);
        }

        // Création du graphique selon le type sélectionné
        Chart chart = null;
        String title = "Répartition des Cours par Niveau";

        switch (chartType) {
            case BAR_CHART:
                chart = createBarChart(coursCountByNiveau, title, "Niveau", "Nombre de Cours");
                break;
            case PIE_CHART:
                chart = createPieChart(coursCountByNiveau, title);
                break;
            case AREA_CHART:
                chart = createAreaChart(coursCountByNiveau, title, "Niveau", "Nombre de Cours");
                break;
            case STACKED_BAR_CHART:
                chart = createStackedBarChart(coursCountByNiveau, title, "Niveau", "Nombre de Cours");
                break;
            case BUBBLE_CHART:
                chart = createBubbleChart(coursCountByNiveau, title, "Niveau", "Nombre de Cours");
                break;
            default:
                chart = createPieChart(coursCountByNiveau, title);
        }

        // Création d'un conteneur pour le graphique avec un titre descriptif
        Label descriptionLabel = new Label(
                "Ce graphique illustre la distribution des cours selon leur niveau de difficulté, permettant d'évaluer l'équilibre de l'offre éducative.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(10));
        descriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

        VBox container = new VBox(10, chart, descriptionLabel);
        container.setPadding(new Insets(15));
        return container;
    }

    /**
     * Crée un graphique montrant la répartition des statuts de cours selon le type
     * de graphique sélectionné
     */
    private VBox createStatusCoursChart(ChartType chartType) {
        // Récupération des données
        List<Cours> allCours = coursService.getAllCours();

        // Comptage des cours par statut
        Map<String, Integer> coursCountByStatus = new HashMap<>();
        for (Cours cours : allCours) {
            String status = cours.getStatusCours() != null ? cours.getStatusCours() : "Non défini";
            coursCountByStatus.put(status, coursCountByStatus.getOrDefault(status, 0) + 1);
        }

        // Création du graphique selon le type sélectionné
        Chart chart = null;
        String title = "Répartition des Cours par Statut";

        switch (chartType) {
            case BAR_CHART:
                chart = createBarChart(coursCountByStatus, title, "Statut", "Nombre de Cours");
                break;
            case PIE_CHART:
                chart = createPieChart(coursCountByStatus, title);
                break;
            case AREA_CHART:
                chart = createAreaChart(coursCountByStatus, title, "Statut", "Nombre de Cours");
                break;
            case STACKED_BAR_CHART:
                chart = createStackedBarChart(coursCountByStatus, title, "Statut", "Nombre de Cours");
                break;
            case BUBBLE_CHART:
                chart = createBubbleChart(coursCountByStatus, title, "Statut", "Nombre de Cours");
                break;
            default:
                chart = createPieChart(coursCountByStatus, title);
        }

        // Création d'un conteneur pour le graphique avec un titre descriptif
        Label descriptionLabel = new Label(
                "Ce graphique montre la distribution des cours selon leur statut (lu, non lu, en cours, etc.), permettant d'évaluer la progression des apprenants.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(10));
        descriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

        VBox container = new VBox(10, chart, descriptionLabel);
        container.setPadding(new Insets(15));
        return container;
    }

    /**
     * Crée un graphique montrant la distribution des matières par module et
     * enseignant selon le type de graphique sélectionné
     */
    private VBox createDistributionMatieresChart(ChartType chartType) {
        // Récupération des données
        List<Matiere> matieres = matiereService.getAllMatieres();

        // Préparation des données pour le graphique
        Map<String, Integer> matieresByModule = new HashMap<>();
        Map<String, Integer> matieresByEnseignant = new HashMap<>();

        // Comptage des matières par module et par enseignant
        for (Matiere matiere : matieres) {
            String module = "Module " + matiere.getModuleId();
            String enseignant = "Enseignant " + matiere.getEnseignantId();

            matieresByModule.put(module, matieresByModule.getOrDefault(module, 0) + 1);
            matieresByEnseignant.put(enseignant, matieresByEnseignant.getOrDefault(enseignant, 0) + 1);
        }

        // Création des graphiques selon le type sélectionné
        Chart moduleChart = null;
        Chart enseignantChart = null;
        String moduleTitle = "Distribution des Matières par Module";
        String enseignantTitle = "Distribution des Matières par Enseignant";

        switch (chartType) {
            case BAR_CHART:
                moduleChart = createBarChart(matieresByModule, moduleTitle, "Module", "Nombre de Matières");
                enseignantChart = createBarChart(matieresByEnseignant, enseignantTitle, "Enseignant",
                        "Nombre de Matières");
                break;
            case PIE_CHART:
                moduleChart = createPieChart(matieresByModule, moduleTitle);
                enseignantChart = createPieChart(matieresByEnseignant, enseignantTitle);
                break;
            case AREA_CHART:
                moduleChart = createAreaChart(matieresByModule, moduleTitle, "Module", "Nombre de Matières");
                enseignantChart = createAreaChart(matieresByEnseignant, enseignantTitle, "Enseignant",
                        "Nombre de Matières");
                break;
            case STACKED_BAR_CHART:
                moduleChart = createStackedBarChart(matieresByModule, moduleTitle, "Module", "Nombre de Matières");
                enseignantChart = createStackedBarChart(matieresByEnseignant, enseignantTitle, "Enseignant",
                        "Nombre de Matières");
                break;
            case BUBBLE_CHART:
                moduleChart = createBubbleChart(matieresByModule, moduleTitle, "Module", "Nombre de Matières");
                enseignantChart = createBubbleChart(matieresByEnseignant, enseignantTitle, "Enseignant",
                        "Nombre de Matières");
                break;
            default:
                moduleChart = createAreaChart(matieresByModule, moduleTitle, "Module", "Nombre de Matières");
                enseignantChart = createAreaChart(matieresByEnseignant, enseignantTitle, "Enseignant",
                        "Nombre de Matières");
        }

        // Création d'un conteneur pour les deux graphiques avec un titre descriptif
        Label descriptionLabel = new Label(
                "Ces graphiques montrent la distribution des matières par module et par enseignant, permettant d'analyser la répartition de la charge de travail et l'organisation des modules.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(10));
        descriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

        VBox container = new VBox(15, moduleChart, enseignantChart, descriptionLabel);
        container.setPadding(new Insets(15));
        return container;
    }

    /**
     * Crée un graphique à barres à partir des données fournies
     */
    private BarChart<String, Number> createBarChart(Map<String, Integer> data, String title, String xAxisLabel,
            String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre");

        // Ajout des données au graphique
        data.forEach((key, value) -> {
            series.getData().add(new XYChart.Data<>(key, value));
        });

        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.getStyleClass().add("custom-bar-chart");

        return barChart;
    }

    /**
     * Crée un graphique en camembert à partir des données fournies
     */
    private PieChart createPieChart(Map<String, Integer> data, String title) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        data.forEach((key, value) -> {
            pieChartData.add(new PieChart.Data(key + " (" + value + ")", value));
        });

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle(title);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.getStyleClass().add("custom-pie-chart");

        // Animation de rotation pour le graphique circulaire
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);

        return pieChart;
    }

    /**
     * Crée un graphique sinusoïdal (area chart) à partir des données fournies
     */
    private AreaChart<String, Number> createAreaChart(Map<String, Integer> data, String title, String xAxisLabel,
            String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle(title);
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre");

        // Ajout des données au graphique
        data.forEach((key, value) -> {
            series.getData().add(new XYChart.Data<>(key, value));
        });

        areaChart.getData().add(series);
        areaChart.setCreateSymbols(true);
        areaChart.getStyleClass().add("custom-area-chart");

        return areaChart;
    }

    /**
     * Crée un graphique à barres empilées à partir des données fournies
     */
    private StackedBarChart<String, Number> createStackedBarChart(Map<String, Integer> data, String title,
            String xAxisLabel, String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle(title);
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Groupe 1");

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Groupe 2");

        // Répartition des données en deux séries pour l'exemple
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (i % 2 == 0) {
                series1.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            } else {
                series2.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            i++;
        }

        stackedBarChart.getData().addAll(series1, series2);
        stackedBarChart.getStyleClass().add("custom-stacked-bar-chart");

        return stackedBarChart;
    }

    /**
     * Crée un graphique à bulles à partir des données fournies
     */
    private BubbleChart<Number, Number> createBubbleChart(Map<String, Integer> data, String title, String xAxisLabel,
            String yAxisLabel) {
        NumberAxis xAxis = new NumberAxis(0, data.size() + 1, 1);
        NumberAxis yAxis = new NumberAxis();
        BubbleChart<Number, Number> bubbleChart = new BubbleChart<>(xAxis, yAxis);
        bubbleChart.setTitle(title);
        xAxis.setLabel("Index");
        yAxis.setLabel(yAxisLabel);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Données");

        // Ajout des données au graphique
        int i = 1;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            // x, y, bubble size
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(i, entry.getValue(), entry.getValue() * 0.5);
            series.getData().add(dataPoint);

            // Ajout d'une étiquette pour chaque bulle
            Label label = new Label(entry.getKey());
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
            dataPoint.setExtraValue(label.getText());

            i++;
        }

        bubbleChart.getData().add(series);
        bubbleChart.getStyleClass().add("custom-bubble-chart");

        return bubbleChart;
    }

    @FXML
    private Button btnRetourMain;

    @FXML
    private void handleRetourMainClick() {
        try {
            // Charger la vue du menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
            Parent root = loader.load();

            // Afficher la vue du menu principal
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnRetourMain.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour au menu principal: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}