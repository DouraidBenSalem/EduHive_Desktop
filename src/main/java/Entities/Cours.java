package Entities;

import java.time.LocalDateTime;

public class Cours {
    private int id;
    private Integer prerequisCoursId;
    private int matiereId;
    private String nomCours;
    private String descriptionCours;
    private int ordre;
    private String statusCours;
    private String niveau;
    private String pdfCours;
    private String imageUrl;
    private LocalDateTime updatedAt;

    // Constructeurs
    public Cours() {}

    public Cours(int id, Integer prerequisCoursId, int matiereId, String nomCours, String descriptionCours, int ordre, String statusCours, String niveau, String pdfCours, String imageUrl, LocalDateTime updatedAt) {
        this.id = id;
        this.prerequisCoursId = prerequisCoursId;
        this.matiereId = matiereId;
        this.nomCours = nomCours;
        this.descriptionCours = descriptionCours;
        this.ordre = ordre;
        this.statusCours = statusCours;
        this.niveau = niveau;
        this.pdfCours = pdfCours;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
    }

    public Cours(Integer prerequisCoursId, int matiereId, String nomCours, String descriptionCours, int ordre, String statusCours, String niveau, String pdfCours, String imageUrl) {
        this.prerequisCoursId = prerequisCoursId;
        this.matiereId = matiereId;
        this.nomCours = nomCours;
        this.descriptionCours = descriptionCours;
        this.ordre = ordre;
        this.statusCours = statusCours;
        this.niveau = niveau;
        this.pdfCours = pdfCours;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Integer getPrerequisCoursId() {
        return prerequisCoursId;
    }

    public int getMatiereId() {
        return matiereId;
    }

    public String getNomCours() {
        return nomCours;
    }

    public String getDescriptionCours() {
        return descriptionCours;
    }

    public int getOrdre() {
        return ordre;
    }

    public String getStatusCours() {
        return statusCours;
    }

    public String getNiveau() {
        return niveau;
    }

    public String getPdfCours() {
        return pdfCours;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPrerequisCoursId(Integer prerequisCoursId) {
        this.prerequisCoursId = prerequisCoursId;
    }

    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }

    public void setNomCours(String nomCours) {
        this.nomCours = nomCours;
    }

    public void setDescriptionCours(String descriptionCours) {
        this.descriptionCours = descriptionCours;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public void setStatusCours(String statusCours) {
        this.statusCours = statusCours;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public void setPdfCours(String pdfCours) {
        this.pdfCours = pdfCours;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
