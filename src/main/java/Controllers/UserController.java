package Controllers;

import Entities.User;
import Services.UserServiceImplementation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.MyDatabase;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;

public class UserController {

    private final UserServiceImplementation userService = new UserServiceImplementation(MyDatabase.getInstance().getConnection());

    @FXML
    private ListView<User> userList;

    @FXML
    private TableColumn<User, Integer> userID;

    @FXML
    private TableColumn<User, String> userPrenom;

    @FXML
    private TableColumn<User, String> userNom;

    @FXML
    private TableColumn<User, String> userEmail;

    @FXML
    private TableColumn<User, String> userRole;

    @FXML
    private TableColumn<User, Void> actionColumn;

    private ObservableList<User> UserList = FXCollections.observableArrayList();

    @FXML
    private TextField addNom;

    @FXML
    private TextField addPrenom;

    @FXML
    private TextField addEmail;

    @FXML
    private PasswordField addPassword;

    @FXML
    private ComboBox<String> addRole;

    @FXML
    private Label addNomError;

    @FXML
    private Label addPrenomError;

    @FXML
    private Label addEmailError;

    @FXML
    private Label addPasswordError;

    @FXML
    private Label addRoleError;

    @FXML
    private Button registerButton;

    @FXML
    private TextField addId;

    private Runnable onSaveCallback;

    @FXML
    private TextField userSearch;

    private FilteredList<User> filteredUserList;

    @FXML
    private HBox userListHeader;

    @FXML
    private Button addProfilePicture;

    private String selectedImagePath;
    private static final String PROFILE_PICTURES_DIR = "src/main/resources/public/profile_pictures/";


    @FXML
    void initialize() {
        if (addRole != null) {
            addRole.getItems().addAll("Etudiant", "Enseignant", "Admin");
            addRole.setValue("Etudiant");
        }

        if (userList != null) {
            if (userListHeader != null) {
                userListHeader.getStyleClass().add("user-list-header");
                userListHeader.getChildren().clear();
                
                Label nomHeaderLabel = new Label("Nom Complet");
                nomHeaderLabel.setPrefWidth(150);
                nomHeaderLabel.getStyleClass().add("header-label");
                
                Label emailHeaderLabel = new Label("Email");
                emailHeaderLabel.setPrefWidth(200);
                emailHeaderLabel.getStyleClass().add("header-label");
                
                Label roleHeaderLabel = new Label("Role");
                roleHeaderLabel.setPrefWidth(120);
                roleHeaderLabel.getStyleClass().add("header-label");
                
                Label actionsHeaderLabel = new Label("Actions");
                actionsHeaderLabel.setPrefWidth(150);
                actionsHeaderLabel.getStyleClass().add("header-label");
                
                userListHeader.getChildren().addAll(nomHeaderLabel, emailHeaderLabel, roleHeaderLabel, actionsHeaderLabel);
            }
            
            userList.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    
                    if (empty || user == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox container = new HBox();
                        container.getStyleClass().add("user-list-cell");
                                                
                        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
                        nameLabel.setPrefWidth(150);
                        
                        Label emailLabel = new Label(user.getEmail());
                        emailLabel.setPrefWidth(200);
                        
                        Label roleLabel = new Label(user.getRole());
                        roleLabel.setPrefWidth(120);
                        
                        Button editButton = new Button("Modifier");
                        editButton.setOnAction(e -> editUser(user));
                        
                        Button deleteButton = new Button("Supprimer");
                        deleteButton.setOnAction(e -> {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                            confirm.setTitle("Confirm Delete");
                            confirm.setHeaderText("Delete User: " + user.getNom() + " " + user.getPrenom());
                            confirm.setContentText("Are you sure you want to delete this user?");
                            
                            confirm.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    userService.deleteUser(user);
                                    getUsersFromDB(); // Refresh the list after deletion
                                }
                            });
                        });
                        
                        HBox buttonsBox = new HBox(10, editButton, deleteButton);
                        buttonsBox.setPrefWidth(150);
                        
                        container.getChildren().addAll(nameLabel, emailLabel, roleLabel, buttonsBox);
                        setGraphic(container);
                    }
                }
            });
            
            setupSearch();
            
            getUsersFromDB();
        }
    }

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
                File directory = new File(PROFILE_PICTURES_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destinationFile = new File(PROFILE_PICTURES_DIR + uniqueFileName);

                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = uniqueFileName;

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

    private void editUser(User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/addUser.fxml"));
            Scene scene = new Scene(loader.load());
            
            UserController controller = loader.getController();
            controller.initData(selectedUser.getId());
            controller.setOnSaveCallback(() -> getUsersFromDB());
    
            Stage stage = (Stage) userList.getScene().getWindow();
            stage.setTitle("Modifier User");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        filteredUserList = new FilteredList<>(UserList, p -> true);
        
        userSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUserList.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty() || newValue.equals("Search...")) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (String.valueOf(user.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getRole().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            });
            
            userList.setItems(filteredUserList);
        });
        
        userSearch.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && userSearch.getText().equals("Search...")) {
                userSearch.setText("");
            } else if (!newValue && (userSearch.getText() == null || userSearch.getText().isEmpty())) {
                userSearch.setText("Search...");
            }
        });
    }

    private void getUsersFromDB() {
        UserList.clear();
        try {
            UserList = FXCollections.observableArrayList(userService.getUsers());
            
            if (filteredUserList == null) {
                filteredUserList = new FilteredList<>(UserList, p -> true);
            } else {
                filteredUserList = new FilteredList<>(UserList, filteredUserList.getPredicate());
            }
            
            if (userList != null) {
                userList.setItems(filteredUserList);
                
                double cellHeight = 50;
                double totalHeight = Math.min(300, cellHeight * filteredUserList.size());
                userList.setPrefHeight(totalHeight);
                userList.setMaxHeight(totalHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addUserOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) userList.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/addUser.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onStatistiqueButton(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userStatistique.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML 
    private void onCancelButton(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) addNom.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void submitUserUpdate() {
        if (!validateUserForm()) {
            return;
        }

        User user = new User(
                Integer.parseInt(addId.getText()),
                addNom.getText(),
                addPrenom.getText(),
                addEmail.getText(),
                convertRole(),
                true
        );
        boolean success;
        if (selectedImagePath != null) {
            user.setProfilePicture(selectedImagePath);
        }

        if (addPassword.getText().isEmpty()) {
            success = userService.updateUser(user);
        } else {
            success = userService.updateUser(user, addPassword.getText());
        }
        if (!success) {
            addRoleError.setText("Probléme serveur");
            return;
        }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) addNom.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    private void initData(int id) {
        User user = userService.getUserById(id);

        registerButton.setOnAction(e -> {
            submitUserUpdate();
        });

        addId.setText(String.valueOf(user.getId()));
        addNom.setText(user.getNom());
        addEmail.setText(user.getEmail());
        addPrenom.setText(user.getPrenom());
        if(user.getRole().equals("ROLE_STUDENT")) {
            addRole.setValue("Etudiant");
        } else if (user.getRole().equals("ROLE_TEACHER")) {
            addRole.setValue("Enseignant");
        } else if (user.getRole().equals("ROLE_ADMIN")) {
            addRole.setValue("Admin");
        }

    }

    @FXML
    private void submitUserFormCreate(ActionEvent event) {
        if (!validateUserForm() || addPassword.getText().equals("")) {
            addPasswordError.setText("Remplir mot de passe");
            return;
        }

        Connection conn = MyDatabase.getInstance().getConnection();
        User user = new User(
                addNom.getText(),
                addPrenom.getText(),
                addEmail.getText(),
                convertRole(),
                true
        );

        user.setPassword(addPassword.getText());
        if (selectedImagePath != null) {
            user.setProfilePicture(selectedImagePath);
        }
        int id = userService.addUser(user);
        if (id == -1) {
            addEmailError.setText("Email déjà utilisé");
            return;
        } else if (id == 0) {
            addRoleError.setText("Probléme serveur");
            return;
        }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) addNom.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    private String convertRole() {
        if(addRole.getValue().equals("Etudiant")) {
            return "ROLE_STUDENT";
        } else if (addRole.getValue().equals("Enseignant")) {
            return "ROLE_TEACHER";
        } else if (addRole.getValue().equals("Admin")) {
            return "ROLE_ADMIN";
        } else {
            return "";
        }
    }

    private Boolean validateUserForm() {
        boolean isValid = true;

        // Clear previous errors
        addPrenomError.setText("");
        addNomError.setText("");
        addEmailError.setText("");
        addPasswordError.setText("");
        addRoleError.setText("");

        // Validate Name
        String name = addPrenom.getText().trim();
        if (name.isEmpty()) {
            addPrenomError.setText("Prenom requis.");
            isValid = false;
        }

        // Validate Surname
        String surname = addNom.getText().trim();
        if (surname.isEmpty()) {
            addNomError.setText("Nom requis.");
            isValid = false;
        }

        // Validate Email
        String email = addEmail.getText().trim();
        if (email.isEmpty()) {
            addEmailError.setText("Email requis.");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            addEmailError.setText("Email valide requis.");
            isValid = false;
        }

        // Validate Password
        String password = addPassword.getText();
        if (password.length() < 6 && !password.isEmpty()) {
            addPasswordError.setText("mot de passe minimum 6 characteres.");
            isValid = false;
        }

        // Validate Role (assuming the text of selected MenuItem is used)
        if ((!(addRole.getValue().equals("Enseignant")) && addRole.getValue().equals("Admin") && !(addRole.getValue().equals("Etudiant"))) || addRole.getValue().isEmpty()) {
            addRoleError.setText("Selectionné un role.");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    void exportertable(ActionEvent event) {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save PDF File");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            java.io.File file = fileChooser.showSaveDialog(source.getScene().getWindow());

            if (file != null) {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 54, 36);
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(44, 62, 80));
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("User List", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add timestamp
                com.itextpdf.text.Font timestampFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC);
                com.itextpdf.text.Paragraph timestamp = new com.itextpdf.text.Paragraph(
                    "Exportée le: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    timestampFont
                );
                timestamp.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                timestamp.setSpacingAfter(20);
                document.add(timestamp);

                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(5);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);
                float[] columnWidths = {0.1f, 0.2f, 0.2f, 0.3f, 0.2f};
                pdfTable.setWidths(columnWidths);

                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(255, 255, 255));
                String[] headers = {"ID", "Nom", "Prénom", "Email", "Role"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(52, 152, 219));
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
                ObservableList<User> items = userList.getItems();
                boolean alternateRow = false;
                for (User u : items) {
                    com.itextpdf.text.BaseColor bgColor = alternateRow ? 
                        new com.itextpdf.text.BaseColor(245, 245, 245) : 
                        new com.itextpdf.text.BaseColor(255, 255, 255);

                    addStyledCell(pdfTable, String.valueOf(u.getId()), contentFont, bgColor);
                    addStyledCell(pdfTable, u.getNom(), contentFont, bgColor);
                    addStyledCell(pdfTable, u.getPrenom(), contentFont, bgColor);
                    addStyledCell(pdfTable, u.getEmail(), contentFont, bgColor);
                    addStyledCell(pdfTable, u.getRole(), contentFont, bgColor);
                    
                    alternateRow = !alternateRow;
                }

                document.add(pdfTable);

                com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph("Généré par EduHive System", footerFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                footer.setSpacingBefore(20);
                document.add(footer);

                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Réussi");
                alert.setHeaderText(null);
                alert.setContentText("Données utilisateur Exportées avec succés!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while exporting to PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void addStyledCell(com.itextpdf.text.pdf.PdfPTable table, String content, com.itextpdf.text.Font font, com.itextpdf.text.BaseColor bgColor) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(content, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        table.addCell(cell);
    }


}
