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

public class User {
    private int id;
    private Integer classeId;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String profilePicture = null;
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

    
}
