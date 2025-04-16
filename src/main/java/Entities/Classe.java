package Entities;

public class Classe {
    private int id;
    private String classename;
    private int num_etudiant;
    
    // 🔹 Constructeur par défaut
    public Classe() {
    }
    
    // 🔹 Constructeur avec tous les champs
    public Classe(int id, String classename, int num_etudiant) {
        this.id = id;
        this.classename = classename;
        this.num_etudiant = num_etudiant;
    }
    
    // 🔹 Constructeur sans id (pour création)
    public Classe(String classename, int num_etudiant) {
        this.classename = classename;
        this.num_etudiant = num_etudiant;
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
    
    @Override
    public String toString() {
        return "Classe{" +
                "id=" + id +
                ", classename='" + classename + '\'' +
                ", num_etudiant=" + num_etudiant +
                '}';
    }
}