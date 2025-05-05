package Services;

import Entities.User;

import java.util.List;


public interface UserService {
    List<User> getUsers();
    int addUser(User user);
    Boolean deleteUser(User user);
    Boolean updateUser(User user);
    Boolean updateUser(User user, String password);
    Boolean updateUserPassword(String password, int userId);
    User getUserById(int userId);
    Boolean loginUser(String email, String password);
    String passwordForgotten(String email);
    List<User> getUsersByClassId(int classeId);
}
