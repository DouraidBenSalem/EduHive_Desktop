package Entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import Services.UserService;
import org.mindrot.jbcrypt.BCrypt;

public class User implements UserService {
    private int id;
    private Integer classeId;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String profilePicture;
    private String phone;
    private LocalDateTime createdAt;
    private String googleAuthenticatorSecret;
    private Boolean isTwoFactorEnabled;
    private LocalDateTime lastLogin;
    private String lastKnownIp;
    private String role;
    private String userType;
    private boolean isApproved;
    private String resetToken;

    // Constructors
    public User() {}

    public User(int id, String nom, String prenom, String email, String role, Boolean isApproved) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.isApproved = isApproved;
    }

    public User(String nom, String prenom, String email, String role, Boolean isApproved) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.isApproved = isApproved;
    }
    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getClasseId() { return classeId; }
    public void setClasseId(Integer classeId) { this.classeId = classeId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGoogleAuthenticatorSecret() { return googleAuthenticatorSecret; }
    public void setGoogleAuthenticatorSecret(String secret) { this.googleAuthenticatorSecret = secret; }

    public Boolean getIsTwoFactorEnabled() { return isTwoFactorEnabled; }
    public void setIsTwoFactorEnabled(Boolean enabled) { this.isTwoFactorEnabled = enabled; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getLastKnownIp() { return lastKnownIp; }
    public void setLastKnownIp(String lastKnownIp) { this.lastKnownIp = lastKnownIp; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    @Override
    public List<User> getUsers(Connection connection) {
        List<User> userList = new ArrayList<>();
        String query = "SELECT id, nom, prenom, role, email FROM user";

        try (PreparedStatement statement = connection.prepareStatement(query);
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
            e.printStackTrace(); // Ideally use a logger
        }

        return userList;
    }

    @Override
    public int addUser(User user, Connection connection) {
        String query = "INSERT INTO user (nom, prenom, role, email, password) VALUES (?, ?, ?, ?, 'abcdefg')";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            statement.executeUpdate();

            String queryId = "SELECT id FROM user WHERE email = ?";
            try (PreparedStatement statement2 = connection.prepareStatement(queryId)) {
                statement2.setString(1, user.getEmail());
                ResultSet resultSet = statement2.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Check if the error is a uniqueness constraint violation
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("unique constraint") || 
                e.getSQLState().equals("23000")) {
                return -1; // Return -1 to indicate a uniqueness violation
            }
            return 0;
        }
    }

    @Override
    public void deleteUser(User user, Connection connection) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean updateUser(User user, Connection connection) {
        String query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            statement.setInt(5, user.getId());

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
    public Boolean updateUser(User user, Connection connection, String password) {
        String query = "UPDATE user SET nom = ?, prenom = ?, role = ?, email = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getEmail());
            statement.setInt(5, user.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0 && updateUserPassword(password, connection)) {
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
    public Boolean updateUserPassword(String password, Connection connection) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "UPDATE user SET password = ? WHERE id = ?";
        
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, hashedPassword);
            statement.setInt(2, getId());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUserById(int userId, Connection connection) {
        String query = "SELECT * FROM user WHERE id = ?";
        User user = null; 
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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
}
