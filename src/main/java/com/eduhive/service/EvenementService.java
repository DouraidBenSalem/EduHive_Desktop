package com.eduhive.service;

import com.eduhive.entity.Evenement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {
    private static final String URL = "jdbc:mysql://localhost:3306/eduhive_database";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void create(Evenement evenement) throws SQLException {
        String query = "INSERT INTO evenement (nom, description, lieu, date, organiseur) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, evenement.getNom());
            pstmt.setString(2, evenement.getDescription());
            pstmt.setString(3, evenement.getLieu());
            pstmt.setString(4, evenement.getDate());
            pstmt.setString(5, evenement.getOrganisateur());
            pstmt.executeUpdate();
        }
    }

    public List<Evenement> readAll() throws SQLException {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Evenement evenement = Evenement.builder()
                    .id(rs.getInt("id"))
                    .nom(rs.getString("nom"))
                    .description(rs.getString("description"))
                    .lieu(rs.getString("lieu"))
                    .date(rs.getString("date"))
                    .organisateur(rs.getString("organiseur"))
                    .build();
                evenements.add(evenement);
            }
        }
        return evenements;
    }

    public Evenement readById(int id) throws SQLException {
        String query = "SELECT * FROM evenement WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Evenement.builder()
                        .id(rs.getInt("id"))
                        .nom(rs.getString("nom"))
                        .description(rs.getString("description"))
                        .lieu(rs.getString("lieu"))
                        .date(rs.getString("date"))
                        .organisateur(rs.getString("organiseur"))
                        .build();
                }
            }
        }
        return null;
    }

    public void update(Evenement evenement) throws SQLException {
        String query = "UPDATE evenement SET nom = ?, description = ?, lieu = ?, date = ?, organiseur = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, evenement.getNom());
            pstmt.setString(2, evenement.getDescription());
            pstmt.setString(3, evenement.getLieu());
            pstmt.setString(4, evenement.getDate());
            pstmt.setString(5, evenement.getOrganisateur());
            pstmt.setInt(6, evenement.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM evenement WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
} 