package Services;

import Entities.Classe;
import java.util.List;

public interface ClasseService {
    List<Classe> getAllClasses();
    Classe getClasseById(int id);
    void addClasse(Classe classe);
    void updateClasse(Classe classe);
    void deleteClasse(int id);
}