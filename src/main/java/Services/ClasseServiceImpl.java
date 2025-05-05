package Services;

import Entities.Classe;
import Entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ClasseServiceImpl implements ClasseService {
    private Connection conn;
    private final UserService userService;
    private final ResultService resultService;

    public ClasseServiceImpl() {
        conn = MyDatabase.getInstance().getConnection();
        userService = new UserServiceImplementation();
        resultService = new ResultServiceImpl();
    }

    @Override
    public List<Classe> getAllClasses() {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Classe classe = new Classe(
                    rs.getInt("id"),
                    rs.getString("classename"),
                    rs.getInt("num_etudiant"),
                    rs.getDouble("classemoy")
                );
                classes.add(classe);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all classes: " + e.getMessage());
        }
        return classes;
    }

    @Override
    public Classe getClasseById(int id) {
        String query = "SELECT * FROM classe WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Classe(
                        rs.getInt("id"),
                        rs.getString("classename"),
                        rs.getInt("num_etudiant"),
                        rs.getDouble("classemoy")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting class by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addClasse(Classe classe) {
        String query = "INSERT INTO classe (classename, num_etudiant, classemoy) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, classe.getClassename());
            pstmt.setInt(2, classe.getNum_etudiant());
            pstmt.setDouble(3, classe.getClassemoy());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding class: " + e.getMessage());
        }
    }

    @Override
    public void updateClasse(Classe classe) {
        String query = "UPDATE classe SET classename = ?, num_etudiant = ?, classemoy = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, classe.getClassename());
            pstmt.setInt(2, classe.getNum_etudiant());
            pstmt.setDouble(3, classe.getClassemoy());
            pstmt.setInt(4, classe.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating class: " + e.getMessage());
        }
    }

    @Override
    public void deleteClasse(int id) {
        try {
            // First, update users to remove the classe_id reference
            String updateUsersQuery = "UPDATE user SET classe_id = NULL WHERE classe_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateUsersQuery)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            // Then delete the classe
            String deleteClasseQuery = "DELETE FROM classe WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteClasseQuery)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting class: " + e.getMessage());
            throw new RuntimeException("Error deleting class: " + e.getMessage());
        }
    }

    @Override
    public void updateClasseAverage(int classeId, double average) {
        String query = "UPDATE classe SET classemoy = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, average);
            pstmt.setInt(2, classeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating class average: " + e.getMessage());
        }
    }

    @Override
    public void advanceAndBalanceClasses() {
        List<Classe> allClasses = getAllClasses();
        Map<String, Classe> nextLevelClasses = new HashMap<>();

        // First pass: Create next level classes if they don't exist
        for (Classe currentClass : allClasses) {
            String nextClassName = getNextClassName(currentClass.getClassename());
            if (nextClassName != null) {
                // Check if next level class already exists
                boolean exists = allClasses.stream()
                    .anyMatch(c -> c.getClassename().equals(nextClassName));
                
                if (!exists) {
                    Classe newClass = new Classe(nextClassName, 0);
                    addClasse(newClass);
                    nextLevelClasses.put(currentClass.getClassename(), getClasseByName(nextClassName));
                } else {
                    nextLevelClasses.put(currentClass.getClassename(), 
                        allClasses.stream()
                            .filter(c -> c.getClassename().equals(nextClassName))
                            .findFirst()
                            .orElse(null));
                }
            }
        }

        // Second pass: Move eligible students
        for (Classe currentClass : allClasses) {
            List<User> students = userService.getUsersByClassId(currentClass.getId())
                .stream()
                .filter(user -> "ROLE_STUDENT".equals(user.getRole()))
                .collect(Collectors.toList());
            
            // Calculate student averages and sort by average
            List<Map.Entry<User, Double>> studentAverages = students.stream()
                .map(student -> Map.entry(student, resultService.calculateStudentAverage(student.getId())))
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

            Classe nextClass = nextLevelClasses.get(currentClass.getClassename());
            if (nextClass != null) {
                // Move students with average >= 9 to next class
                for (Map.Entry<User, Double> entry : studentAverages) {
                    if (entry.getValue() >= 9.0) {
                        moveStudentToClass(entry.getKey().getId(), nextClass.getId());
                    }
                }
            }
        }

        // Third pass: Balance classes at each level
        Map<String, List<Classe>> classesByYearAndSection = allClasses.stream()
            .collect(Collectors.groupingBy(c -> {
                String className = c.getClassename();
                // Group by first character (year level) and everything after first character
                return className.substring(0, 1); // e.g., "4" for "4a46"
            }));

        // Balance classes within each year level
        for (List<Classe> yearClasses : classesByYearAndSection.values()) {
            if (yearClasses.size() > 1) {
                balanceClassesAtLevel(yearClasses);
            }
        }
    }

    private void balanceClassesAtLevel(List<Classe> classes) {
        // Get all students in these classes
        List<Map.Entry<User, Double>> allStudents = new ArrayList<>();
        for (Classe classe : classes) {
            List<User> students = userService.getUsersByClassId(classe.getId())
                .stream()
                .filter(user -> "ROLE_STUDENT".equals(user.getRole()))
                .collect(Collectors.toList());
            for (User student : students) {
                double avg = resultService.calculateStudentAverage(student.getId());
                if (avg >= 9.0) { // Only consider passing students for balancing
                    allStudents.add(Map.entry(student, avg));
                }
            }
        }

        // Sort students by average
        allStudents.sort(Map.Entry.<User, Double>comparingByValue().reversed());

        // Distribute students evenly among classes
        int studentsPerClass = allStudents.size() / classes.size();
        int currentClassIndex = 0;

        for (int i = 0; i < allStudents.size(); i++) {
            User student = allStudents.get(i).getKey();
            Classe targetClass = classes.get(currentClassIndex);
            moveStudentToClass(student.getId(), targetClass.getId());

            if ((i + 1) % studentsPerClass == 0 && currentClassIndex < classes.size() - 1) {
                currentClassIndex++;
            }
        }
    }

    @Override
    public String getNextClassName(String currentName) {
        if (currentName == null || currentName.length() < 2) {
            return null;
        }

        int currentLevel = Character.getNumericValue(currentName.charAt(0));
        if (currentLevel >= 5) { // Max level is 5
            return null;
        }

        return (currentLevel + 1) + currentName.substring(1);
    }

    @Override
    public void moveStudentToClass(int studentId, int newClassId) {
        String query = "UPDATE user SET classe_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, newClassId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error moving student to new class: " + e.getMessage());
        }
    }

    private Classe getClasseByName(String className) {
        String query = "SELECT * FROM classe WHERE classename = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, className);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Classe classe = new Classe();
                classe.setId(rs.getInt("id"));
                classe.setClassename(rs.getString("classename"));
                classe.setNum_etudiant(rs.getInt("num_etudiant"));
                classe.setClassemoy(rs.getDouble("classemoy"));
                return classe;
            }
        } catch (SQLException e) {
            System.err.println("Error getting class by name: " + e.getMessage());
        }
        return null;
    }
}