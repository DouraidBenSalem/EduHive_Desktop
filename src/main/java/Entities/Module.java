package Entities;

public class Module {
    private int id;
    private String nom_module;
    private String description_module;
    private String module_img;
    
    // 🔹 Constructeur par défaut
    public Module() {
    }
    
    // 🔹 Constructeur avec tous les champs
    public Module(int id, String nom_module, String description_module, String module_img) {
        this.id = id;
        this.nom_module = nom_module;
        this.description_module = description_module;
        this.module_img = module_img;
    }
    
    // 🔹 Constructeur sans id (pour création)
    public Module(String nom_module, String description_module, String module_img) {
        this.nom_module = nom_module;
        this.description_module = description_module;
        this.module_img = module_img;
    }
    
    // 🔹 Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom_module() {
        return nom_module;
    }
    
    public void setNom_module(String nom_module) {
        this.nom_module = nom_module;
    }
    
    public String getDescription_module() {
        return description_module;
    }
    
    public void setDescription_module(String description_module) {
        this.description_module = description_module;
    }
    
    public String getModule_img() {
        return module_img;
    }
    
    public void setModule_img(String module_img) {
        this.module_img = module_img;
    }
    
    
    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", nom_module='" + nom_module + '\'' +
                ", description_module='" + description_module + '\'' +
                ", module_img='" + module_img + '\'' +
                '}';
    }
}