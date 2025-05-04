package Entities;

public class Matiere {
    private int id;
    private int moduleId;
    private int enseignantId;
    private String nomMatiere;
    private String descriptionMatiere;
    private Integer prerequisMatiere; // Using Integer to allow null values
    private String objectifMatiere;
    private String imageUrl; // URL de l'image associée à la matière
    
    // Constructeur par défaut
    public Matiere() {
    }
    
    // Constructeur avec tous les champs
    public Matiere(int id, int moduleId, int enseignantId, String nomMatiere, String descriptionMatiere, 
                  Integer prerequisMatiere, String objectifMatiere, String imageUrl) {
        this.id = id;
        this.moduleId = moduleId;
        this.enseignantId = enseignantId;
        this.nomMatiere = nomMatiere;
        this.descriptionMatiere = descriptionMatiere;
        this.prerequisMatiere = prerequisMatiere;
        this.objectifMatiere = objectifMatiere;
        this.imageUrl = imageUrl;
    }
    
    // Constructeur sans ID pour les nouvelles matières
    public Matiere(int moduleId, int enseignantId, String nomMatiere, String descriptionMatiere, 
                  Integer prerequisMatiere, String objectifMatiere, String imageUrl) {
        this.moduleId = moduleId;
        this.enseignantId = enseignantId;
        this.nomMatiere = nomMatiere;
        this.descriptionMatiere = descriptionMatiere;
        this.prerequisMatiere = prerequisMatiere;
        this.objectifMatiere = objectifMatiere;
        this.imageUrl = imageUrl;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }
    
    public int getEnseignantId() {
        return enseignantId;
    }
    
    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }
    
    public String getNomMatiere() {
        return nomMatiere;
    }
    
    public void setNomMatiere(String nomMatiere) {
        this.nomMatiere = nomMatiere;
    }
    
    public String getDescriptionMatiere() {
        return descriptionMatiere;
    }
    
    public void setDescriptionMatiere(String descriptionMatiere) {
        this.descriptionMatiere = descriptionMatiere;
    }
    
    public Integer getPrerequisMatiere() {
        return prerequisMatiere;
    }
    
    public void setPrerequisMatiere(Integer prerequisMatiere) {
        this.prerequisMatiere = prerequisMatiere;
    }
    
    public String getObjectifMatiere() {
        return objectifMatiere;
    }
    
    public void setObjectifMatiere(String objectifMatiere) {
        this.objectifMatiere = objectifMatiere;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}