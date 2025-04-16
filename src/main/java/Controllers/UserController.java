package Controllers;

import Entities.User;
import Entities.quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.MyDatabase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class UserController {

    @FXML
    private TableView<User> userTable;

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
    void initialize() {
        if (addRole != null) {
            addRole.getItems().addAll("Etudiant", "Enseignant", "Admin");
            addRole.setValue("Etudiant");
        }

        if(userTable != null) {
            userID.setCellValueFactory(new PropertyValueFactory<>("id"));
            userNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            userPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            userEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            userRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    
            getUsersFromDB();
    
            if (actionColumn == null) {
                actionColumn = new TableColumn<>("Actions");
                userTable.getColumns().add(actionColumn);
            }
    
            addActionButtonsToTable();
    
        }
    }

    private void getUsersFromDB() {
        UserList.clear();
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            User user = new User();
            UserList = FXCollections.observableArrayList(user.getUsers(conn));
            userTable.setItems(UserList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addUserOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) userTable.getScene().getWindow();
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

    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/addUser.fxml"));
                        Scene scene = new Scene(loader.load());
                        
                        UserController controller = loader.getController();
                        controller.initData(selectedUser.getId());
                        controller.setOnSaveCallback(() -> getUsersFromDB());

                        Stage stage = (Stage) userTable.getScene().getWindow();
                        stage.setTitle("Modifier User");
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                btnDelete.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    Connection conn = MyDatabase.getInstance().getConnection();
                    selectedUser.deleteUser(selectedUser, conn);
                    getUsersFromDB();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void submitUserUpdate() {
        if (!validateUserForm()) {
            return;
        }

        Connection conn = MyDatabase.getInstance().getConnection();
        User user = new User(
                Integer.parseInt(addId.getText()),
                addNom.getText(),
                addPrenom.getText(),
                addEmail.getText(),
                convertRole(),
                true
        );
        boolean success;
        if (addPassword.getText() != null) {
            success = user.updateUser(user, conn);
        } else {
            success = user.updateUser(user, conn, addPassword.getText());
        }
        
        if(success) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controllers/userPage.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) addNom.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            addRoleError.setText("Probléme serveur");
        }
    }

    private void initData(int id) {
        Connection conn = MyDatabase.getInstance().getConnection();
        User user = User.getUserById(id, conn);

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

        int id = user.addUser(user, conn);
        if (id == -1) {
            addEmailError.setText("Email déjà utilisé");
            return;
        }
        user.setId(id);
        Boolean success = user.updateUserPassword(addPassword.getText(), conn);
        if(success) {
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
                // Set up document with margins
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 54, 36);
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                // Add styled header with logo
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

                // Create and style table
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(5);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);
                float[] columnWidths = {0.1f, 0.2f, 0.2f, 0.3f, 0.2f};
                pdfTable.setWidths(columnWidths);

                // Style headers
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(255, 255, 255));
                String[] headers = {"ID", "Nom", "Prénom", "Email", "Role"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(52, 152, 219));
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                // Style content cells
                com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
                ObservableList<User> items = userTable.getItems();
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

                // Add footer
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

    // Helper method to add styled cells to the PDF table
    private void addStyledCell(com.itextpdf.text.pdf.PdfPTable table, String content, com.itextpdf.text.Font font, com.itextpdf.text.BaseColor bgColor) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(content, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        table.addCell(cell);
    }
}
