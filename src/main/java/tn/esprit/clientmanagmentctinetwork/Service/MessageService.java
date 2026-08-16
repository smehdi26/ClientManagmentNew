package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Model.MessageModel;
import java.util.List;

public interface MessageService {
    void sendMessage(String from, String to, String subject, String content);
    List<MessageModel> getInbox(String email);
    void markAsRead(Long id);
    void deleteMessage(Long id);
    long getUnreadCount(String email);
}