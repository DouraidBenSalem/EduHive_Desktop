package Controllers;

import Entities.Result;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.ResultService;
import services.ResultServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatistiqueController {

    @FXML
    private NavBarController navbarController;

    @FXML
    private BarChart<String, Number> noteBarChart;

    @FXML
    private PieChart reponsesChart;

    @FXML
    private Button retourButton;

    @FXML
    private Label statistiqueTitle;

    private ResultService resultService = new ResultServiceImpl();
    private ObservableList<Result> resultList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        if (navbarController != null) {

        }

        loadResultsFromDB();
        initializeCharts();
    }

    private void loadResultsFromDB() {
        resultList.clear();
        resultList.addAll(resultService.getAllResults());
    }

    private void initializeCharts() {

        initializeNoteBarChart();


        initializeReponsesChart();
    }

    private void initializeNoteBarChart() {

        CategoryAxis xAxis = (CategoryAxis) noteBarChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) noteBarChart.getYAxis();
        xAxis.setLabel("Notes");
        yAxis.setLabel("Nombre d'étudiants");


        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Distribution des notes");


        Map<Integer, Integer> noteDistribution = new HashMap<>();
        for (Result result : resultList) {
            int note = result.getNote();
            noteDistribution.put(note, noteDistribution.getOrDefault(note, 0) + 1);
        }


        for (int i = 0; i <= 20; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), noteDistribution.getOrDefault(i, 0)));
        }


        noteBarChart.getData().add(series);


        noteBarChart.setAnimated(true);
        noteBarChart.setTitle("Distribution des Notes (/20)");
    }

    private void initializeReponsesChart() {

        int totalCorrect = 0;
        int totalIncorrect = 0;

        for (Result result : resultList) {
            totalCorrect += result.getNbRepCorrect();
            totalIncorrect += result.getNbRepIncorrect();
        }


        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Réponses Correctes", totalCorrect),
                new PieChart.Data("Réponses Incorrectes", totalIncorrect)
        );

        int total = totalCorrect + totalIncorrect;
        if (total > 0) {
            String correctLabel = String.format("Réponses Correctes: %.1f%%", (totalCorrect * 100.0 / total));
            String incorrectLabel = String.format("Réponses Incorrectes: %.1f%%", (totalIncorrect * 100.0 / total));
            pieChartData.get(0).setName(correctLabel);
            pieChartData.get(1).setName(incorrectLabel);
        }


        reponsesChart.setData(pieChartData);
        reponsesChart.setTitle("Répartition des Réponses");
        reponsesChart.setLabelsVisible(true);
        reponsesChart.setAnimated(true);



    }

    @FXML
    void retourResultats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ResultController.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.setTitle("Résultats");
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de naviguer vers la page des résultats: " + e.getMessage());
            alert.showAndWait();
        }
    }
}