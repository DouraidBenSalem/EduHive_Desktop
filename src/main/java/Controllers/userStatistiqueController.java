package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.MyDatabase;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class userStatistiqueController implements Initializable {
    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private PieChart pieChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadChartData();
    }

    private void loadChartData() {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT DATE(created_at) as date, COUNT(*) as count FROM user GROUP BY DATE(created_at) ORDER BY date";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            // Data for Bar Chart and Line Chart
            XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            barSeries.setName("Nouveau utilisateur journalier");
            lineSeries.setName("Tendance des inscriptions");

            Map<String, Integer> monthlyData = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (rs.next()) {
                String date = rs.getDate("date").toLocalDate().format(formatter);
                int count = rs.getInt("count");

                barSeries.getData().add(new XYChart.Data<>(date, count));
                lineSeries.getData().add(new XYChart.Data<>(date, count));

                String month = date.substring(0, 7); // Get YYYY-MM
                monthlyData.merge(month, count, Integer::sum);
            }

            barChart.getData().clear();
            barChart.getData().add(barSeries);

            lineChart.getData().clear();
            lineChart.getData().add(lineSeries);

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            pieChart.setData(pieChartData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showBarChart() {
        barChart.setVisible(true);
        lineChart.setVisible(false);
        pieChart.setVisible(false);
    }

    @FXML
    private void showLineChart() {
        barChart.setVisible(false);
        lineChart.setVisible(true);
        pieChart.setVisible(false);
    }

    @FXML
    private void showPieChart() {
        barChart.setVisible(false);
        lineChart.setVisible(false);
        pieChart.setVisible(true);
    }
}