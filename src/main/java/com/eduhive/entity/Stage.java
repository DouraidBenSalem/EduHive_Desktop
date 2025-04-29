package com.eduhive.entity;

public class Stage {
    private Integer id;
    private String titre;
    private String entreprise;
    private String description;
    private String duree;

    public Stage() {}

    public Stage(Integer id, String titre, String entreprise, String description, String duree) {
        this.id = id;
        this.titre = titre;
        this.entreprise = entreprise;
        this.description = description;
        this.duree = duree;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }
} 