package Entities;

public class quiz {
    private int id;
    private String titre;
    private String question;
    private String repCorrect;
    private String optionA;
    private String optionB;

    // 🔹 Constructeur par défaut
    public quiz() {
    }

    // 🔹 Constructeur avec tous les champs
    public quiz(int id, String titre, String question, String repCorrect, String optionA, String optionB) {
        this.id = id;
        this.titre = titre;
        this.question = question;
        this.repCorrect = repCorrect;
        this.optionA = optionA;
        this.optionB = optionB;
    }

    // 🔹 Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRepCorrect() {
        return repCorrect;
    }

    public void setRepCorrect(String repCorrect) {
        this.repCorrect = repCorrect;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }
}
