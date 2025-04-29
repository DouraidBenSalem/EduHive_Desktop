package Controllers;

import Entities.User;
import Main.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import Services.UserServiceImplementation;
import javafx.stage.Stage;
import utils.MyDatabase;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    private final UserServiceImplementation userService = new UserServiceImplementation(MyDatabase.getInstance().getConnection());
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

    @FXML
    private TextField forgottenEmail;

    @FXML
    private Label forgottenError;

    @FXML
    private Button resetCancel;

    @FXML
    private Button resetConfirm;

    @FXML
    private PasswordField resetPassword;

    @FXML
    private Label resetPasswordError;

    @FXML
    private TextField resetToken;

    @FXML
    private Label resetTokenError;

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

        if (!userService.loginUser(email, password)) {
            loginPasswordError.setText("Email ou mot de passe incorrect.");
            return;
        }
        try {
            successLogin();
        } catch (IOException e) {
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

        User user = new User(surname, name, email, role, true);
        user.setPassword(password);

        int addError = userService.addUser(user);

        if (addError == -1) {
            registerEmailError.setText("Cette adresse mail existe déja");
            return;
        } else if (addError == 0) {
            registerRoleError.setText("Erreur serveur.");
            return;
        } else if (addError == 1) {
            try {
                goToLogin();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void forgottenButtonOnAction(ActionEvent event) {
        String email = forgottenEmail.getText();
        String response = userService.passwordForgotten(email);
        if (response.equals("")) {
            forgottenError.setText("Email invalide.");
        } else {
            forgottenError.setText("un email a été envoyé.");
        }
    }

    @FXML
    private void forgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Controllers/passwordForgotten.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void forgotConfirmation(ActionEvent event) {
        if (forgottenEmail.getText().isEmpty()) {
            forgottenError.setText("Email requis.");
            return;
        } else if (!forgottenEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            forgottenError.setText("Email valide requis.");
            return;
        }
        String response = userService.passwordForgotten(forgottenEmail.getText());

        if (response.equals("not found")) {
            forgottenError.setText("Email introuvable.");
        } else if (response.equals("failure"))  {
            forgottenError.setText("une erreur s'est produite.");
        } else if (response.equals("success")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Controllers/passwordReset.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void resetConfirmation(ActionEvent event) {
        resetPasswordError.setText("");
        resetTokenError.setText("");

        if(!validateReset()) {
            return;
        }

        String token = resetToken.getText();
        String password = resetPassword.getText();

        Boolean response = userService.resetPassword(token, password);

        if (response) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Controllers/login.fxml"));
                Stage stage = (Stage) ((Node) resetConfirm).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            resetTokenError.setText("Code invalide.");
        }
    }

    private Boolean validateReset() {
        Boolean isValid = true;
        if (resetToken.getText().isEmpty()) {
            resetTokenError.setText("Code requis.");
            isValid = false;
        }
        if (resetPassword.getText().isEmpty()) {
            resetPasswordError.setText("Mot de passe requis.");
            isValid = false;
        } else if (resetPassword.getText().length() < 8) {
            resetPasswordError.setText("Mot de passe minimum 8 characteres.");
            isValid = false;
        }
        return isValid;
    }

}
