package Services;

import Entities.Matiere;
import java.util.List;

public interface MatiereService {
    List<Matiere> getAllMatieres();

    Matiere getMatiereById(int id);

    void addMatiere(Matiere matiere);

    void updateMatiere(Matiere matiere);

    void deleteMatiere(int id);

    List<Matiere> getMatieresByModuleId(int moduleId);

    List<Matiere> getMatieresByEnseignantId(int enseignantId);
}