package Controllers;

import Main.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;


public class LoginController implements Initializable {
    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label loginEmailError;

    @FXML
    private Label loginPasswordError;

    @FXML
    private ComboBox<String> registerRoleComboBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button registerCancelButton;

    @FXML
    private Label registerEmailError;

    @FXML
    private TextField registerEmailField;

    @FXML
    private Label registerNameError;

    @FXML
    private TextField registerNameField;

    @FXML
    private Label registerPasswordError;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label registerRoleError;

    @FXML
    private Label registerSurnameError;

    @FXML
    private TextField registerSurnameField;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (registerRoleComboBox != null) {
            registerRoleComboBox.getItems().addAll("Etudiant", "Enseignant");
            registerRoleComboBox.setValue("Etudiant");
        }
    }

    @FXML
    private void successLogin() throws IOException {
            Stage currentStage = (Stage) loginEmailField.getScene().getWindow();
            currentStage.close();
            Main mainApp = new Main();
            mainApp.start(new Stage());
    }

    public void loginButtonOnAction(ActionEvent event) {
        if (!validateLogin()) {
            return;
        }
            String email = loginEmailField.getText();
            String password = loginPasswordField.getText();

        MyDatabase connectNow = MyDatabase.getInstance();
        Connection con = connectNow.getConnection();

        String query = "SELECT password FROM user WHERE email = ?";
        try {
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (hashedPassword.startsWith("$2y$")) {
                    hashedPassword = hashedPassword.replaceFirst("\\$2y\\$", "\\$2a\\$");
                }
                if (BCrypt.checkpw(password, hashedPassword)) {

                    successLogin();
                } else {
                    // Wrong password
                    loginPasswordError.setText(" Email ou mot de passe incorrect");
                }
            } else {
                // Email not found
                loginPasswordError.setText(" Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerButtonOnAction(ActionEvent event) {
        if (!validateRegister()) {
            return;
        }

        String name = registerNameField.getText().trim();
        String surname = registerSurnameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();
        String role = "";
        if(registerRoleComboBox.getValue().equals("Etudiant")) {
            role = "ROLE_STUDENT";
        } else if (registerRoleComboBox.getValue().equals("Enseignant")) {
            role = "ROLE_TEACHER";
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            MyDatabase connectNow = MyDatabase.getInstance();
            Connection con = connectNow.getConnection();

            String sql = "INSERT INTO user (nom, prenom, email, password, role, user_type, is_approved, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, role);
            stmt.setString(6, "user");
            stmt.setInt(7, 1); // not approved by default
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            goToLogin();

        }catch (SQLIntegrityConstraintViolationException ex) {
            // Specific exception for duplicate key
            if (ex.getMessage().contains("UNIQ_8D93D649E7927C74")) {
                registerEmailError.setText("cet email existe deja.");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loginRegisterButtonOnAction(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Controllers/register.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void registerCancelButtonOnAction(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Controllers/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void goToLogin() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Controllers/login.fxml"));

        Stage stage = (Stage) registerCancelButton.getScene().getWindow(); // or use any other node in the registration form
        stage.setScene(new Scene(root));
        stage.show();
    }

    public boolean validateLogin() {
        boolean isValid = true;
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        if (email.isEmpty()) {
            isValid = false;
            loginEmailError.setText("Email requis");
        } else if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            isValid = false;
            loginEmailError.setText("Email valide requis");
        } else {
            loginEmailError.setText("");
        }

        if (password.isEmpty()) {
            isValid = false;
            loginPasswordError.setText("Mot de passe requis");
        } else {
            loginPasswordError.setText("");
        }

        return isValid;
    }

    public Boolean validateRegister() {
        boolean isValid = true;

        // Clear previous errors
        registerNameError.setText("");
        registerSurnameError.setText("");
        registerEmailError.setText("");
        registerPasswordError.setText("");
        registerRoleError.setText("");

        // Validate Name
        String name = registerNameField.getText().trim();
        if (name.isEmpty()) {
            registerNameError.setText("Prenom requis.");
            isValid = false;
        }

        // Validate Surname
        String surname = registerSurnameField.getText().trim();
        if (surname.isEmpty()) {
            registerSurnameError.setText("Nom requis.");
            isValid = false;
        }

        // Validate Email
        String email = registerEmailField.getText().trim();
        if (email.isEmpty()) {
            registerEmailError.setText("Email requis.");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            registerEmailError.setText("Email valide requis.");
            isValid = false;
        }

        // Validate Password
        String password = registerPasswordField.getText();
        if (password.isEmpty()) {
            registerPasswordError.setText("mot de passe requis.");
            isValid = false;
        } else if (password.length() < 6) {
            registerPasswordError.setText("mot de passe minimum 6 characteres.");
            isValid = false;
        }

        // Validate Role (assuming the text of selected MenuItem is used)
        if ((!(registerRoleComboBox.getValue().equals("Enseignant")) && !(registerRoleComboBox.getValue().equals("Etudiant"))) || registerRoleComboBox.getValue().isEmpty()) {
            registerRoleError.setText("Selectionné un role.");
            isValid = false;
        }

        return isValid;
    }

}
