package Services;

import Entities.quiz;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuizServiceImpl implements QuizService {
    
    @Override
    public List<quiz> getAllQuizzes() {
        List<quiz> quizList = new ArrayList<>();
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM quiz";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                quiz q = new quiz(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("question"),
                        rs.getString("rep_correct"),
                        rs.getString("option_a"),
                        rs.getString("option_b")
                );
                quizList.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quizList;
    }

    @Override
    public quiz getQuizById(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM quiz WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new quiz(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("question"),
                        rs.getString("rep_correct"),
                        rs.getString("option_a"),
                        rs.getString("option_b")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addQuiz(quiz q) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "INSERT INTO quiz (titre, question, rep_correct, option_a, option_b) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, q.getTitre());
            pstmt.setString(2, q.getQuestion());
            pstmt.setString(3, q.getRepCorrect());
            pstmt.setString(4, q.getOptionA());
            pstmt.setString(5, q.getOptionB());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateQuiz(quiz q) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "UPDATE quiz SET titre = ?, question = ?, rep_correct = ?, option_a = ?, option_b = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, q.getTitre());
            pstmt.setString(2, q.getQuestion());
            pstmt.setString(3, q.getRepCorrect());
            pstmt.setString(4, q.getOptionA());
            pstmt.setString(5, q.getOptionB());
            pstmt.setInt(6, q.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuiz(int id) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            String query = "DELETE FROM quiz WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}