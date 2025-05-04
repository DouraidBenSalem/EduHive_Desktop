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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.MyDatabase;
import org.apache.http.entity.ContentType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;

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

    @FXML
    private Button addProfilePicture;

    private String selectedImagePath;
    private static final String PROFILE_PICTURES_DIR = "src/main/resources/public/profile_pictures/";

    @FXML
    private Button faceIdLogin;

    @FXML
    private void handleProfilePictureSelection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(addProfilePicture.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create profile pictures directory if it doesn't exist
                File directory = new File(PROFILE_PICTURES_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Generate unique filename
                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destinationFile = new File(PROFILE_PICTURES_DIR + uniqueFileName);

                // Copy file to profile pictures directory
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = uniqueFileName;

                // Update button text to show selected
                addProfilePicture.setText("Picture Selected");
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save profile picture.");
                alert.showAndWait();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (registerRoleComboBox != null) {
            registerRoleComboBox.getItems().addAll("Etudiant", "Enseignant");
            registerRoleComboBox.setValue("Etudiant");
        }
        
        if (addProfilePicture != null) {
            addProfilePicture.setOnAction(this::handleProfilePictureSelection);
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
        if (registerRoleComboBox.getValue().equals("Etudiant")) {
            role = "ROLE_STUDENT";
        } else if (registerRoleComboBox.getValue().equals("Enseignant")) {
            role = "ROLE_TEACHER";
        }

        User user = new User(surname, name, email, role, true);
        user.setPassword(password);
        if (selectedImagePath != null) {
            user.setProfilePicture(selectedImagePath);
        }

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

        Stage stage = (Stage) registerCancelButton.getScene().getWindow(); // or use any other node in the registration
                                                                           // form
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

        registerNameError.setText("");
        registerSurnameError.setText("");
        registerEmailError.setText("");
        registerPasswordError.setText("");
        registerRoleError.setText("");

        String name = registerNameField.getText().trim();
        if (name.isEmpty()) {
            registerNameError.setText("Prenom requis.");
            isValid = false;
        }

        String surname = registerSurnameField.getText().trim();
        if (surname.isEmpty()) {
            registerSurnameError.setText("Nom requis.");
            isValid = false;
        }

        String email = registerEmailField.getText().trim();
        if (email.isEmpty()) {
            registerEmailError.setText("Email requis.");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            registerEmailError.setText("Email valide requis.");
            isValid = false;
        }

        String password = registerPasswordField.getText();
        if (password.isEmpty()) {
            registerPasswordError.setText("mot de passe requis.");
            isValid = false;
        } else if (password.length() < 6) {
            registerPasswordError.setText("mot de passe minimum 6 characteres.");
            isValid = false;
        }

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

    @FXML
    public Boolean validateEmail() {
        Boolean isValid = true;
        String email = loginEmailField.getText();
        if (email.isEmpty()) {
            isValid = false;
            loginEmailError.setText("Email requis");
        } else if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            isValid = false;
            loginEmailError.setText("Email valide requis");
        } else {
            loginEmailError.setText("");
        }
        return isValid;
    }

    @FXML
    private void handleFaceIdLoginOnAction(ActionEvent event) {

        if (!validateEmail()) {
            return;
        }

        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                loginPasswordError.setText("Aucune camera detectée");
                return;
            }

            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();

            BufferedImage image = webcam.getImage();
            webcam.close();

            if (image == null) {
                loginPasswordError.setText("Identification image echouée");
                return;
            }

            File tempDir = new File("src/main/resources/temp/");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            File tempFile = new File(tempDir, "temp_capture.jpg");
            ImageIO.write(image, "JPG", tempFile);


            Boolean res = userService.compareFaces(loginEmailField.getText(), tempFile.getPath());
            tempFile.delete();

            if (res) {
                successLogin();
            } else {
                loginPasswordError.setText("identification faciale echouée");
            }

        } catch (IOException e) {
            e.printStackTrace();
            loginPasswordError.setText("une erreur s'est produite");
        }
    }
}
