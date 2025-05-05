package Services;

import Entities.Matiere;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereServiceImpl implements MatiereService {
    private Connection conn;

    public MatiereServiceImpl() {
        // Obtenir la connexion à la base de données
        conn = MyDatabase.getInstance().getConnection();
    }

    @Override
    public List<Matiere> getAllMatieres() {
        List<Matiere> matieres = new ArrayList<>();
        try {
            String query = "SELECT * FROM matiere";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Matiere matiere = extractMatiereFromResultSet(rs);
                matieres.add(matiere);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving matieres: " + e.getMessage());
        }
        return matieres;
    }

    @Override
    public Matiere getMatiereById(int id) {
        try {
            String query = "SELECT * FROM matiere WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMatiereFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving matiere by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addMatiere(Matiere matiere) {
        try {
            String query = "INSERT INTO matiere (module_id, enseignant_id, nom_matiere, description_matiere, prerequis_matiere, objectif_matiere) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, matiere.getModuleId());
            pstmt.setInt(2, matiere.getEnseignantId());
            pstmt.setString(3, matiere.getNomMatiere());
            pstmt.setString(4, matiere.getDescriptionMatiere());

            if (matiere.getPrerequisMatiere() != null) {
                pstmt.setInt(5, matiere.getPrerequisMatiere());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, matiere.getObjectifMatiere());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Récupérer l'ID généré et le définir dans l'objet matiere
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    matiere.setId(generatedKeys.getInt(1));
                }
                System.out.println("Matiere added successfully with ID: " + matiere.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error adding matiere: " + e.getMessage());
        }
    }

    @Override
    public void updateMatiere(Matiere matiere) {
        try {
            String query = "UPDATE matiere SET module_id = ?, enseignant_id = ?, nom_matiere = ?, " +
                    "description_matiere = ?, prerequis_matiere = ?, objectif_matiere = ? " +
                    "WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, matiere.getModuleId());
            pstmt.setInt(2, matiere.getEnseignantId());
            pstmt.setString(3, matiere.getNomMatiere());
            pstmt.setString(4, matiere.getDescriptionMatiere());

            if (matiere.getPrerequisMatiere() != null) {
                pstmt.setInt(5, matiere.getPrerequisMatiere());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, matiere.getObjectifMatiere());
            pstmt.setInt(7, matiere.getId());

            pstmt.executeUpdate();
            System.out.println("Matiere updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating matiere: " + e.getMessage());
        }
    }

    @Override
    public void deleteMatiere(int id) {
        try {
            String query = "DELETE FROM matiere WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Matiere deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting matiere: " + e.getMessage());
        }
    }

    @Override
    public List<Matiere> getMatieresByModuleId(int moduleId) {
        List<Matiere> matieres = new ArrayList<>();
        try {
            String query = "SELECT * FROM matiere WHERE module_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, moduleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Matiere matiere = extractMatiereFromResultSet(rs);
                matieres.add(matiere);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving matieres by module ID: " + e.getMessage());
        }
        return matieres;
    }

    @Override
    public List<Matiere> getMatieresByEnseignantId(int enseignantId) {
        List<Matiere> matieres = new ArrayList<>();
        try {
            String query = "SELECT * FROM matiere WHERE enseignant_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, enseignantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Matiere matiere = extractMatiereFromResultSet(rs);
                matieres.add(matiere);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving matieres by enseignant ID: " + e.getMessage());
        }
        return matieres;
    }

    private Matiere extractMatiereFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int moduleId = rs.getInt("module_id");
        int enseignantId = rs.getInt("enseignant_id");
        String nomMatiere = rs.getString("nom_matiere");
        String descriptionMatiere = rs.getString("description_matiere");

        Integer prerequisMatiere = rs.getInt("prerequis_matiere");
        if (rs.wasNull()) {
            prerequisMatiere = null;
        }

        String objectifMatiere = rs.getString("objectif_matiere");

        return new Matiere(id, moduleId, enseignantId, nomMatiere, descriptionMatiere, prerequisMatiere,
                objectifMatiere);
    }
}