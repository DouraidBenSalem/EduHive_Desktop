package Entities;

public class Result {
    private int id;
    private int userId;
    private String userName; // To display user name instead of just ID
    private int note;
    private String commentaire;
    private int nbRepCorrect;
    private int nbRepIncorrect;
    private Integer quizId; // Using Integer to allow null values
    private String quizTitle; // To display quiz title instead of just ID

    public Result() {
    }

    public Result(int id, int userId, String userName, int note, String commentaire, int nbRepCorrect, int nbRepIncorrect, Integer quizId, String quizTitle) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.note = note;
        this.commentaire = commentaire;
        this.nbRepCorrect = nbRepCorrect;
        this.nbRepIncorrect = nbRepIncorrect;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
    }

    // Constructor without ID for new results
    public Result(int userId, String userName, int note, String commentaire, int nbRepCorrect, int nbRepIncorrect, Integer quizId, String quizTitle) {
        this.userId = userId;
        this.userName = userName;
        this.note = note;
        this.commentaire = commentaire;
        this.nbRepCorrect = nbRepCorrect;
        this.nbRepIncorrect = nbRepIncorrect;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNbRepCorrect() {
        return nbRepCorrect;
    }

    public void setNbRepCorrect(int nbRepCorrect) {
        this.nbRepCorrect = nbRepCorrect;
    }

    public int getNbRepIncorrect() {
        return nbRepIncorrect;
    }

    public void setNbRepIncorrect(int nbRepIncorrect) {
        this.nbRepIncorrect = nbRepIncorrect;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", nbRepCorrect=" + nbRepCorrect +
                ", nbRepIncorrect=" + nbRepIncorrect +
                ", quizId=" + quizId +
                ", quizTitle='" + quizTitle + '\'' +
                '}';
    }
}