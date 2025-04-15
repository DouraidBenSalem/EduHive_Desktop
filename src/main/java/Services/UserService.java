package Services;

import Entities.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    void addUser(User user);
    void deleteUser(User user);
    void updateUser(User user);
}
