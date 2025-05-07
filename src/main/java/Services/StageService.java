package Services;

import Entities.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StageService {
    private static final String URL = "jdbc:mysql://localhost:3306/eduhive_databas";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void create(Stage stage) throws SQLException {
        String query = "INSERT INTO stage (titre, entreprise, description, duree) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, stage.getTitre());
            pstmt.setString(2, stage.getEntreprise());
            pstmt.setString(3, stage.getDescription());
            pstmt.setString(4, stage.getDuree());
            pstmt.executeUpdate();
        }
    }

    public List<Stage> readAll() throws SQLException {
        List<Stage> stages = new ArrayList<>();
        String query = "SELECT * FROM stage";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Stage stage = new Stage(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("entreprise"),
                    rs.getString("description"),
                    rs.getString("duree")
                );
                stages.add(stage);
            }
        }
        return stages;
    }

    public Stage readById(int id) throws SQLException {
        String query = "SELECT * FROM stage WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Stage(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("entreprise"),
                        rs.getString("description"),
                        rs.getString("duree")
                    );
                }
            }
        }
        return null;
    }

    public void update(Stage stage) throws SQLException {
        String query = "UPDATE stage SET titre = ?, entreprise = ?, description = ?, duree = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, stage.getTitre());
            pstmt.setString(2, stage.getEntreprise());
            pstmt.setString(3, stage.getDescription());
            pstmt.setString(4, stage.getDuree());
            pstmt.setInt(5, stage.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM stage WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
} 