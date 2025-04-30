package Services;

import Entities.User;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;
import org.apache.http.entity.ContentType;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;


public class UserServiceImplementation implements UserService {
    private Connection connection;
    private static final String USERNAME = "5bf6148201b374";
    private static final String PASSWORD = "a92955d27b9fef";
    private static final String HOST = "sandbox.smtp.mailtrap.io";
    private static final int PORT = 2525;
    private static final String API_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";
    private static final String API_KEY = "hIfoMnQlKfQFwQhoYE3UnIXeeHainkLh";
    private static final String API_SECRET = "2jOA2o1-nVKZy9WkMRnVsj4rXlcH1M3i";
    private static final String PROFILE_PICTURES_DIR = "src/main/resources/public/profile_pictures/";


    public UserServiceImplementation(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT id, nom, prenom, role, email FROM user";

        try (PreparedStatement statement = this.connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                user.setRole(resultSet.getString("role"));
                user.setEmail(resultSet.getString("email"));

                userList.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userList;
    }

    @Override
    public int addUser(User user) {
        String password = user.getPassword();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String query = "INSERT INTO user (nom, prenom, role, email, password, profile_picture) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            statement.setString(5, hashedPassword);
            statement.setString(6, user.getProfilePicture());
            statement.executeUpdate();

            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("unique constraint") || 
                e.getSQLState().equals("23000")) {
                return -1;
            }
            return 0;
        }
    }

    @Override
    public Boolean deleteUser(User user) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean updateUser(User user) {
        String query = null;
        if (user.getProfilePicture() == null) {
            query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ? WHERE id = ?";
        } else {
            query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ?, profile_picture = ? WHERE id = ?";
        }


        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            if (user.getProfilePicture() != null) {
                statement.setString(5, user.getProfilePicture());
                statement.setInt(6, user.getId());
            } else {
                statement.setInt(5, user.getId());
            }

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override 
    public Boolean updateUser(User user, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        String query = null;
        if (user.getProfilePicture() == null) {
            query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ?, password = ? WHERE id = ?";
        } else {
            query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ?, password = ?, profile_picture = ? WHERE id = ?";
        }

        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPassword());
            if (user.getProfilePicture() != null) {
                statement.setString(6, user.getProfilePicture());
                statement.setInt(7, user.getId());
            } else {
                statement.setInt(6, user.getId());
            }

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public Boolean updateUserPassword(String password, int id) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "UPDATE user SET password = ? WHERE id = ?";
        
        try {
            PreparedStatement statement = this.connection.prepareStatement(query);
            statement.setString(1, hashedPassword);
            statement.setInt(2, id);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE id = ?";
        User user = null; 
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                user.setRole(resultSet.getString("role"));
                user.setEmail(resultSet.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean loginUser(String email, String password) {
        String query = "SELECT password FROM user WHERE email = ?";
        try {
            PreparedStatement pst = this.connection.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (hashedPassword.startsWith("$2y$")) {
                    hashedPassword = hashedPassword.replaceFirst("\\$2y\\$", "\\$2a\\$");
                }
                return BCrypt.checkpw(password, hashedPassword);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String passwordForgotten(String email) {
        // First check if email exists
        String checkEmailQuery = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement checkStatement = this.connection.prepareStatement(checkEmailQuery)) {
            checkStatement.setString(1, email);
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                return "failure";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "failure";
        }

        String token = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String query = "UPDATE user SET reset_token =? WHERE email =?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, token);
            statement.setString(2, email);
            statement.executeUpdate();
            if (sendEmail(email, "Password reset", token)) {
                return "success";
            }
            
            return "failure";
        } catch (SQLException e) {
            e.printStackTrace();
            return "failure";
        }
    }

    public boolean sendEmail(String to, String subject, String token) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@eduhive.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject("EduHive - " + subject);
            
            // Create HTML content for email
            String htmlContent = 
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='background-color: #0066cc; padding: 20px; text-align: center;'>" +
                "<h1 style='color: white; margin: 0;'>EduHive</h1>" +
                "</div>" +
                "<div style='padding: 20px; background-color: #f5f5f5;'>" +
                "<h2 style='color: #0066cc;'>Demande de Réinitialisation de Mot de Passe</h2>" +
                "<p>Vous avez demandé à réinitialiser votre mot de passe. Veuillez utiliser le jeton suivant pour compléter le processus :</p>" +
                "<div style='background-color: white; padding: 15px; margin: 20px 0; border-radius: 5px; text-align: center;'>" +
                "<span style='font-size: 24px; color: #0066cc; font-weight: bold;'>" + token + "</span>" +
                "</div>" +
                "<p>Si vous n'avez pas demandé cette réinitialisation de mot de passe, veuillez ignorer cet e-mail.</p>" +
                "</div>" +
                "<div style='background-color: #0066cc; color: white; padding: 15px; text-align: center;'>" +
                "<p style='margin: 0;'>© 2024 EduHive. Tous droits réservés.</p>" +
                "</div>" +
                "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean resetPassword(String token, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "UPDATE user SET password =?, reset_token =? WHERE reset_token =?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(query);
            statement.setString(1, hashedPassword);
            statement.setString(2, null);
            statement.setString(3, token);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        User user = null;
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                user.setRole(resultSet.getString("role"));
                user.setEmail(resultSet.getString("email"));
                user.setProfilePicture(resultSet.getString("profile_picture"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean compareFaces(String email, String userPicturePath) {
        User user = getUserByEmail(email);
        if (user == null || user.getProfilePicture() == null) {
            return false;
        }
        String profilePicturePath = user.getProfilePicture();

        File profilePicFile = new File(PROFILE_PICTURES_DIR + profilePicturePath);
        File userPicFile = new File(userPicturePath);

        if (!profilePicFile.exists() || !userPicFile.exists()) {
            return false;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("api_key", API_KEY);
            entityBuilder.addTextBody("api_secret", API_SECRET);
            entityBuilder.addBinaryBody("image_file1", profilePicFile, ContentType.IMAGE_JPEG, profilePicFile.getName());
            entityBuilder.addBinaryBody("image_file2", userPicFile, ContentType.IMAGE_JPEG, userPicFile.getName());

            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    System.err.println(response.toString());
                    System.err.println("Face comparison API returned status code: " + statusCode);
                    
                    return false;
                }

                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJson = new JSONObject(responseString);

                if (!responseJson.has("confidence")) {
                    System.err.println("Face comparison API response missing confidence value");
                    return false;
                }

                double similarity = responseJson.getDouble("confidence");
                return similarity > 80.0;
            } catch (IOException | JSONException e) {
                System.err.println("Error processing API response: " + e.getMessage());
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error executing HTTP request: " + e.getMessage());
            return false;
        }
    }

}
