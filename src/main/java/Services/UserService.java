package Services;

import Entities.User;

import java.util.List;
import java.sql.Connection;


public interface UserService {
    List<User> getUsers(Connection connection);
    int addUser(User user, Connection connection);
    void deleteUser(User user, Connection connection);
    Boolean updateUser(User user, Connection connection);
    Boolean updateUser(User user, Connection connection, String password);
    Boolean updateUserPassword(String password, Connection connection);
}
