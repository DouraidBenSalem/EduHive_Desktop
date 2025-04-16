package Services;

import Entities.Classe;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClasseServiceImpl implements ClasseService {
    
    @Override
    public List<Classe> getAllClasses() {
        List<Classe> classeList = new ArrayList<>();
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM classe";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Classe classe = new Classe(
                        rs.getInt("id"),
                        rs.getString("classename"),
                        rs.getInt("num_etudiant")
                );
                classeList.add(classe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classeList;
    }

    @Override
    public Classe getClasseById(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM classe WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Classe(
                        rs.getInt("id"),
                        rs.getString("classename"),
                        rs.getInt("num_etudiant")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addClasse(Classe classe) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "INSERT INTO classe (classename, num_etudiant) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, classe.getClassename());
            pstmt.setInt(2, classe.getNum_etudiant());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateClasse(Classe classe) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "UPDATE classe SET classename = ?, num_etudiant = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, classe.getClassename());
            pstmt.setInt(2, classe.getNum_etudiant());
            pstmt.setInt(3, classe.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteClasse(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "DELETE FROM classe WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}