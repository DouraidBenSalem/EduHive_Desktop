package Entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Classe {
    private int id;
    private String classename;
    private int num_etudiant;
    private List<User> users;
    private double classemoy;
    
    // 🔹 Constructeur par défaut
    public Classe() {
        this.users = new ArrayList<>();
    }
    
    // 🔹 Constructeur avec tous les champs
    public Classe(int id, String classename, int num_etudiant, double classemoy) {
        this.id = id;
        this.classename = classename;
        this.num_etudiant = num_etudiant;
        this.users = new ArrayList<>();
        this.classemoy = classemoy;
    }
    
    // 🔹 Constructeur sans id (pour création)
    public Classe(String classename, int num_etudiant) {
        this.classename = classename;
        this.num_etudiant = num_etudiant;
        this.users = new ArrayList<>();
        this.classemoy = 0.0;
    }
    
    // 🔹 Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getClassename() {
        return classename;
    }
    
    public void setClassename(String classename) {
        this.classename = classename;
    }
    
    public int getNum_etudiant() {
        return num_etudiant;
    }
    
    public void setNum_etudiant(int num_etudiant) {
        this.num_etudiant = num_etudiant;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public double getClassemoy() {
        return classemoy;
    }
    
    public void setClassemoy(double classemoy) {
        this.classemoy = classemoy;
    }
    
    // Get students (users with ROLE_STUDENT)
    public List<User> getStudents() {
        return users.stream()
                .filter(user -> "ROLE_STUDENT".equals(user.getRole()))
                .collect(Collectors.toList());
    }
    
    // Get teachers (users with ROLE_TEACHER)
    public List<User> getTeachers() {
        return users.stream()
                .filter(user -> "ROLE_TEACHER".equals(user.getRole()))
                .collect(Collectors.toList());
    }
    
    // Add a user to the class
    public void addUser(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
        }
    }
    
    // Remove a user from the class
    public void removeUser(User user) {
        users.remove(user);
    }
    
    @Override
    public String toString() {
        return "Classe{" +
                "id=" + id +
                ", classename='" + classename + '\'' +
                ", num_etudiant=" + num_etudiant +
                ", students=" + getStudents().size() +
                ", teachers=" + getTeachers().size() +
                ", classemoy=" + classemoy +
                '}';
    }
}