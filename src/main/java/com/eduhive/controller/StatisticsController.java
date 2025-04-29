package com.eduhive.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.eduhive.service.AnnonceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class StatisticsController implements Initializable {
    @FXML private PieChart categoryChart;
    @FXML private VBox statsContainer;

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Group announcements by category and count them
            Map<String, Long> categoryCounts = annonceService.readAll().stream()
                .collect(Collectors.groupingBy(
                    annonce -> annonce.getCategorie(),
                    Collectors.counting()
                ));

            // Create pie chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            categoryCounts.forEach((category, count) -> {
                pieChartData.add(new PieChart.Data(category + " (" + count + ")", count));
            });

            // Update pie chart
            categoryChart.setData(pieChartData);
            categoryChart.setTitle("Répartition des Annonces par Catégorie");

            // Add detailed statistics
            statsContainer.getChildren().clear();
            long total = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
            
            categoryCounts.forEach((category, count) -> {
                double percentage = (count * 100.0) / total;
                Label statLabel = new Label(String.format("%s: %d (%.1f%%)", 
                    category, count, percentage));
                statLabel.setStyle("-fx-font-size: 14;");
                statsContainer.getChildren().add(statLabel);
            });

            // Add total count
            Label totalLabel = new Label(String.format("Total des annonces: %d", total));
            totalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            statsContainer.getChildren().add(totalLabel);

        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message
            Label errorLabel = new Label("Erreur lors du chargement des statistiques: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            statsContainer.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) categoryChart.getScene().getWindow()).close();
    }
} 