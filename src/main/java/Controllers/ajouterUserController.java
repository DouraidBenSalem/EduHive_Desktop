package Controllers;

import Entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import Entities.quiz;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDatabase;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class ajouterUserController {

    @FXML
    private Button ajouter;

    @FXML
    private Button effacer;

    @FXML
    private Button exit;

    @FXML
    private TextField userSurname;

    @FXML
    private TextField userName;

    @FXML
    private TextField userEmail;

    @FXML
    private TextField userPassword;

    @FXML
    private ComboBox<String> userRole;

    private boolean isEditMode = false;
    private User userToEdit;
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void initData(User u) {
        if (u != null) {
            this.userToEdit = u;
            this.isEditMode = true;
            ajouter.setText("Modifier");
            userName.setText(u.getPrenom());
            userSurname.setText(u.getNom());
            userEmail.setText(u.getEmail());
            userPassword.setText("");
            userRole.setValue(u.getRole());
        }
    }

    @FXML
    void ajouterUser(ActionEvent event) {
        String n = userSurname.getText();
        String p = userName.getText();
        String e = userEmail.getText();
        String r = userRole.getValue();
        String pass = userPassword.getText();

        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            if (isEditMode && userToEdit != null) {
                String sql;
                if (pass == null) {
                    sql = "UPDATE User SET nom=?, prenom=?, email=?, role=? WHERE id=?";
                } else {
                    pass = BCrypt.hashpw(pass, BCrypt.gensalt());
                    sql = "UPDATE User SET nom=?, prenom=?, email=?, role=?, password=? WHERE id=?";
                }

                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, n);
                pst.setString(2, p);
                pst.setString(3, e);
                pst.setString(4, r);
                if (pass == null) {
                    pst.setString(5, pass);
                    pst.setInt(6, userToEdit.getId());
                } else {
                    pst.setInt(6, userToEdit.getId());
                }
                pst.executeUpdate();
            } else {
                pass = BCrypt.hashpw(pass, BCrypt.gensalt());
                String sql = "INSERT INTO quiz (nom, prenom, email, role, password) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, n);
                pst.setString(2, p);
                pst.setString(3, e);
                pst.setString(4, r);
                pst.setString(5, pass);
                pst.executeUpdate();
            }

            if (onSaveCallback != null) onSaveCallback.run();

            // Fermer la fenêtre
            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.close();

        } catch (Exception exp) {
            exp.printStackTrace();
            showError("Erreur : " + exp.getMessage());
        }
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    void effaceUser(ActionEvent event) {
        userEmail.clear();
        userPassword.clear();
        userName.clear();
        userSurname.clear();
    }
}
