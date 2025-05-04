package Services;

import Entities.Cours;
import java.util.List;

public interface CoursService {
    void addCours(Cours cours);

    void updateCours(Cours cours);

    void deleteCours(int id);

    Cours getCoursById(int id);

    List<Cours> getAllCours();

    List<Cours> getCoursByMatiereId(Integer matiereId);
}
