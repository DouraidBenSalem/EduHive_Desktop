package services;

import Entities.Module;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ModuleServiceImpl implements ModuleService {
    
    @Override
    public List<Module> getAllModules() {
        List<Module> moduleList = new ArrayList<>();
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM module";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Module module = new Module(
                        rs.getInt("id"),
                        rs.getString("nom_module"),
                        rs.getString("description_module"),
                        rs.getString("module_img"),
                        rs.getDouble("moy")
                );
                moduleList.add(module);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleList;
    }

    @Override
    public Module getModuleById(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM module WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Module(
                        rs.getInt("id"),
                        rs.getString("nom_module"),
                        rs.getString("description_module"),
                        rs.getString("module_img"),
                        rs.getDouble("moy")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addModule(Module module) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "INSERT INTO module (nom_module, description_module, module_img, moy) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, module.getNom_module());
            pstmt.setString(2, module.getDescription_module());
            pstmt.setString(3, module.getModule_img());
            pstmt.setDouble(4, module.getMoy());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateModule(Module module) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "UPDATE module SET nom_module = ?, description_module = ?, module_img = ?, moy = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, module.getNom_module());
            pstmt.setString(2, module.getDescription_module());
            pstmt.setString(3, module.getModule_img());
            pstmt.setDouble(4, module.getMoy());
            pstmt.setInt(5, module.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteModule(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "DELETE FROM module WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}