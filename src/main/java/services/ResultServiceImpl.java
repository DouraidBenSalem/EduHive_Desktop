package Services;

import Entities.Result;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultServiceImpl implements ResultService {
    private Connection conn;

    public ResultServiceImpl() {
        // Make sure this matches the method name in MyDatabase
        conn = MyDatabase.getInstance().getConnection();
    }

    @Override
    public List<Result> getAllResults() {
        List<Result> results = new ArrayList<>();
        try {
            String query = "SELECT r.*, u.nom AS user_name, q.titre AS quiz_title " +
                          "FROM resultat r " +
                          "LEFT JOIN user u ON r.user_id = u.id " +
                          "LEFT JOIN quiz q ON r.quiz_id = q.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Result result = extractResultFromResultSet(rs);
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving results: " + e.getMessage());
        }
        return results;
    }

    @Override
    public Result getResultById(int id) {
        try {
            String query = "SELECT r.*, u.nom AS user_name, q.titre AS quiz_title " +
                          "FROM resultat r " +
                          "LEFT JOIN user u ON r.user_id = u.id " +
                          "LEFT JOIN quiz q ON r.quiz_id = q.id " +
                          "WHERE r.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractResultFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving result by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addResult(Result result) {
        try {
            String query = "INSERT INTO resultat (user_id, note, commentaire, nb_rep_correct, nb_rep_incorrect, quiz_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, result.getUserId());
            pstmt.setInt(2, result.getNote());
            pstmt.setString(3, result.getCommentaire());
            pstmt.setInt(4, result.getNbRepCorrect());
            pstmt.setInt(5, result.getNbRepIncorrect());
            
            if (result.getQuizId() != null) {
                pstmt.setInt(6, result.getQuizId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID and set it to the result object
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getInt(1));
                }
                System.out.println("Result added successfully with ID: " + result.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error adding result: " + e.getMessage());
        }
    }

    @Override
    public void updateResult(Result result) {
        try {
            String query = "UPDATE resultat SET user_id = ?, note = ?, commentaire = ?, " +
                          "nb_rep_correct = ?, nb_rep_incorrect = ?, quiz_id = ? " +
                          "WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, result.getUserId());
            pstmt.setInt(2, result.getNote());
            pstmt.setString(3, result.getCommentaire());
            pstmt.setInt(4, result.getNbRepCorrect());
            pstmt.setInt(5, result.getNbRepIncorrect());
            
            if (result.getQuizId() != null) {
                pstmt.setInt(6, result.getQuizId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.setInt(7, result.getId());
            pstmt.executeUpdate();
            System.out.println("Result updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating result: " + e.getMessage());
        }
    }

    @Override
    public void deleteResult(int id) {
        try {
            String query = "DELETE FROM resultat WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Result deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting result: " + e.getMessage());
        }
    }

    @Override
    public List<Result> getResultsByUserId(int userId) {
        List<Result> results = new ArrayList<>();
        try {
            String query = "SELECT r.*, u.nom AS user_name, q.titre AS quiz_title " +
                          "FROM resultat r " +
                          "LEFT JOIN user u ON r.user_id = u.id " +
                          "LEFT JOIN quiz q ON r.quiz_id = q.id " +
                          "WHERE r.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Result result = extractResultFromResultSet(rs);
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving results by user ID: " + e.getMessage());
        }
        return results;
    }

    @Override
    public List<Result> getResultsByQuizId(int quizId) {
        List<Result> results = new ArrayList<>();
        try {
            String query = "SELECT r.*, u.nom AS user_name, q.titre AS quiz_title " +
                          "FROM resultat r " +
                          "LEFT JOIN user u ON r.user_id = u.id " +
                          "LEFT JOIN quiz q ON r.quiz_id = q.id " +
                          "WHERE r.quiz_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, quizId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Result result = extractResultFromResultSet(rs);
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving results by quiz ID: " + e.getMessage());
        }
        return results;
    }

    @Override
    public double calculateStudentAverage(int studentId) {
        String query = "SELECT note FROM resultat WHERE user_id = ?";
        double sum = 0.0;
        int count = 0;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sum += rs.getDouble("note");
                    count++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If student has no grades, return 0
        return count > 0 ? sum / count : 0.0;
    }

    @Override
    public double calculateClassAverage(List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return 0.0;
        }

        double totalSum = 0.0;
        int totalStudents = studentIds.size(); // Count all students, even those with no grades
        
        // Calculate sum of all grades
        for (int studentId : studentIds) {
            double studentAverage = calculateStudentAverage(studentId);
            totalSum += studentAverage; // Include zeros in the sum
        }
        
        // Divide by total number of students (including those with zero)
        return totalStudents > 0 ? totalSum / totalStudents : 0.0;
    }

    private Result extractResultFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String userName = rs.getString("user_name");
        int note = rs.getInt("note");
        String commentaire = rs.getString("commentaire");
        int nbRepCorrect = rs.getInt("nb_rep_correct");
        int nbRepIncorrect = rs.getInt("nb_rep_incorrect");
        
        Integer quizId = rs.getInt("quiz_id");
        if (rs.wasNull()) {
            quizId = null;
        }
        
        String quizTitle = rs.getString("quiz_title");
        
        return new Result(id, userId, userName, note, commentaire, nbRepCorrect, nbRepIncorrect, quizId, quizTitle);
    }
}