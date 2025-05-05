package Services;

import Entities.Result;
import java.util.List;

public interface ResultService {
    List<Result> getAllResults();
    Result getResultById(int id);
    void addResult(Result result);
    void updateResult(Result result);
    void deleteResult(int id);
    List<Result> getResultsByUserId(int userId);
    List<Result> getResultsByQuizId(int quizId);
    double calculateStudentAverage(int userId);
    double calculateClassAverage(List<Integer> studentIds);
}