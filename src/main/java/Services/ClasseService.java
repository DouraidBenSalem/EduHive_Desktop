package Services;

import Entities.Classe;
import java.util.List;

public interface ClasseService {
    List<Classe> getAllClasses();
    Classe getClasseById(int id);
    void addClasse(Classe classe);
    void updateClasse(Classe classe);
    void deleteClasse(int id);
    void updateClasseAverage(int classeId, double average);
    
    // New methods for class advancement and balancing
    void advanceAndBalanceClasses();
    String getNextClassName(String currentName);
    void moveStudentToClass(int studentId, int newClassId);
}