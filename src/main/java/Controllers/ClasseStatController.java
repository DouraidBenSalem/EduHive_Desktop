package Controllers;

import Entities.Classe;
import Services.ClasseService;
import Services.ClasseServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.util.List;
import java.util.DoubleSummaryStatistics;

public class ClasseStatController {
    @FXML
    private NavBarController navbarController;

    @FXML
    private BarChart<String, Number> classChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label bestClassLabel;

    @FXML
    private Label worstClassLabel;

    @FXML
    private Label averageLabel;

    @FXML
    private Label totalClassesLabel;

    private ClasseService classeService = new ClasseServiceImpl();

    @FXML
    void initialize() {
        navbarController.setParent(this);
        loadStatistics();
    }

    private void loadStatistics() {
        List<Classe> classes = classeService.getAllClasses();
        
        // Clear previous data
        classChart.getData().clear();
        
        // Create a new series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne par Classe");

        // Calculate statistics
        DoubleSummaryStatistics stats = classes.stream()
                .mapToDouble(Classe::getClassemoy)
                .summaryStatistics();

        // Find best and worst classes
        Classe bestClass = classes.stream()
                .max((c1, c2) -> Double.compare(c1.getClassemoy(), c2.getClassemoy()))
                .orElse(null);

        Classe worstClass = classes.stream()
                .min((c1, c2) -> Double.compare(c1.getClassemoy(), c2.getClassemoy()))
                .orElse(null);

        // Add data to chart
        for (Classe classe : classes) {
            series.getData().add(new XYChart.Data<>(
                classe.getClassename(), 
                classe.getClassemoy()
            ));
        }
        classChart.getData().add(series);

        // Calculate percentages and rankings
        double overallAverage = stats.getAverage();
        int totalClasses = classes.size();
        
        // Update labels with professional formatting
        if (bestClass != null) {
            double percentAboveAvg = ((bestClass.getClassemoy() - overallAverage) / overallAverage) * 100;
            bestClassLabel.setText(String.format("Performance Exceptionnelle : %s\nMoyenne : %.2f/20 (%.1f%% au-dessus de la moyenne)", 
                bestClass.getClassename(), bestClass.getClassemoy(), percentAboveAvg));
        }
        
        if (worstClass != null) {
            double percentBelowAvg = ((overallAverage - worstClass.getClassemoy()) / overallAverage) * 100;
            worstClassLabel.setText(String.format("Nécessite Attention : %s\nMoyenne : %.2f/20 (%.1f%% en dessous de la moyenne)", 
                worstClass.getClassename(), worstClass.getClassemoy(), percentBelowAvg));
        }
        
        // Calculate performance distribution
        long classesAboveAvg = classes.stream()
                .filter(c -> c.getClassemoy() > overallAverage)
                .count();
        double percentageAboveAvg = (classesAboveAvg * 100.0) / totalClasses;
        
        averageLabel.setText(String.format("Performance Globale : %.2f/20\n%.1f%% des classes dépassent cette moyenne", 
            overallAverage, percentageAboveAvg));
            
        totalClassesLabel.setText(String.format("Analyse basée sur %d classes actives", totalClasses));
    }

    public void setParent(ClasseStatController parent) {
        // For NavBarController communication
    }
}
