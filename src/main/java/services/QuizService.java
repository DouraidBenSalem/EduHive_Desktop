package Services;

import Entities.quiz;
import java.util.List;

public interface QuizService {
    List<quiz> getAllQuizzes();
    quiz getQuizById(int id);
    void addQuiz(quiz q);
    void updateQuiz(quiz q);
    void deleteQuiz(int id);
}