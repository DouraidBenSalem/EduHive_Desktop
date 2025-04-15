package Services;

import Entities.Message;
import Entities.User;
import java.util.List;
import java.sql.Connection;

public interface MessageService {
    List<Message> getMessages(Connection connection);
    Message getMessageById(Connection connection, int id);
    int addMessage(Message message, Connection connection);
    void deleteMessage(Message message, Connection connection);
    Boolean updateMessage(Message message, Connection connection);
    Boolean updateMessagePassword(String password, Connection connection);
    List<User> getAllUsers(Connection connection);
}
