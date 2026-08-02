package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import java.util.List;

public interface NotificationService {
    void createNotification(String message, String type);
    List<NotificationModel> getAllNotifications();
    long getUnreadCount();
    void markAllAsRead();
    void logDailySummaryIfNew(long count);
    void deleteNotification(Long id);
    void createNotification(String message, String type, String category);
}