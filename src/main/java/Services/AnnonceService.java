package Services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Entities.Annonce;

public class AnnonceService {
    private static final String URL = "jdbc:mysql://localhost:3306/eduhive_databas";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void create(Annonce annonce) throws SQLException {
        String query = "INSERT INTO anonce (titre, description, categorie) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, annonce.getTitre());
            pstmt.setString(2, annonce.getDescription());
            pstmt.setString(3, annonce.getCategorie());
            pstmt.executeUpdate();
        }
    }

    public List<Annonce> readAll() throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        String query = "SELECT * FROM anonce";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Annonce annonce = new Annonce(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getString("categorie")
                );
                annonces.add(annonce);
            }
        }
        return annonces;
    }

    public Annonce readById(int id) throws SQLException {
        String query = "SELECT * FROM anonce WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Annonce(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("categorie")
                    );
                }
            }
        }
        return null;
    }

    public void update(Annonce annonce) throws SQLException {
        String query = "UPDATE anonce SET titre = ?, description = ?, categorie = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, annonce.getTitre());
            pstmt.setString(2, annonce.getDescription());
            pstmt.setString(3, annonce.getCategorie());
            pstmt.setInt(4, annonce.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM anonce WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
} 