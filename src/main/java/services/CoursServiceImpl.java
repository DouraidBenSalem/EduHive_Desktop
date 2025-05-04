package Services;

import Entities.Cours;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursServiceImpl implements CoursService {
    private Connection conn;

    public CoursServiceImpl() {
        // Correction : utilisez la même classe que dans MatiereServiceImpl
        conn = MyDatabase.getInstance().getConnection();

    }

    @Override
    public void addCours(Cours cours) {
        String sql = "INSERT INTO cours (prerequis_cours_id, matiere_id, nom_cours, description_cours, ordre, status_cours, niveau, pdf_cours, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, cours.getPrerequisCoursId(), java.sql.Types.INTEGER);
            ps.setInt(2, cours.getMatiereId());
            ps.setString(3, cours.getNomCours());
            ps.setString(4, cours.getDescriptionCours());
            ps.setInt(5, cours.getOrdre());
            ps.setString(6, cours.getStatusCours());
            ps.setString(7, cours.getNiveau());
            ps.setString(8, cours.getPdfCours());
            ps.setString(9, cours.getImageUrl());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCours(Cours cours) {
        String sql = "UPDATE cours SET prerequis_cours_id=?, matiere_id=?, nom_cours=?, description_cours=?, ordre=?, status_cours=?, niveau=?, pdf_cours=?, image_url=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, cours.getPrerequisCoursId(), java.sql.Types.INTEGER);
            ps.setInt(2, cours.getMatiereId());
            ps.setString(3, cours.getNomCours());
            ps.setString(4, cours.getDescriptionCours());
            ps.setInt(5, cours.getOrdre());
            ps.setString(6, cours.getStatusCours());
            ps.setString(7, cours.getNiveau());
            ps.setString(8, cours.getPdfCours());
            ps.setString(9, cours.getImageUrl());
            ps.setInt(10, cours.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCours(int id) {
        String sql = "DELETE FROM cours WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Cours getCoursById(int id) {
        String sql = "SELECT * FROM cours WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCours(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Cours> getAllCours() {
        return getCoursByMatiereId(null);
    }

    @Override
    public List<Cours> getCoursByMatiereId(Integer matiereId) {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        if (matiereId != null) {
            sql += " WHERE matiere_id = ?";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (matiereId != null) {
                ps.setInt(1, matiereId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToCours(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Cours mapResultSetToCours(ResultSet rs) throws SQLException {
        return new Cours(
                rs.getInt("id"),
                (Integer) rs.getObject("prerequis_cours_id"),
                rs.getInt("matiere_id"),
                rs.getString("nom_cours"),
                rs.getString("description_cours"),
                rs.getInt("ordre"),
                rs.getString("status_cours"),
                rs.getString("niveau"),
                rs.getString("pdf_cours"),
                rs.getString("image_url"),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
    }
}
