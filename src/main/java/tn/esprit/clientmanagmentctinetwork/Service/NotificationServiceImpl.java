package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createNotification(String message, String type, String category) {
        NotificationModel notification = new NotificationModel();
        notification.setMessage(message);
        notification.setType(type);
        notification.setCategory(category); // Save the category
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        notificationRepository.save(notification);
    }

    // Overload for backward compatibility with older logs
    @Override
    public void createNotification(String message, String type) {
        this.createNotification(message, type, "CLIENT");
    }

    @Override
    public List<NotificationModel> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public long getUnreadCount() {
        return notificationRepository.countByReadStatusFalse();
    }

    @Override
    public void markAllAsRead() {
        List<NotificationModel> notifications = notificationRepository.findAll();
        for (NotificationModel n : notifications) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void logDailySummaryIfNew(long count) {
        String dateStr = java.time.LocalDate.now().toString(); // Format: YYYY-MM-DD
        boolean alreadyLogged = notificationRepository.existsDailySummaryForDate(dateStr);
        if (!alreadyLogged) {
            createNotification(
                    "Daily Agenda Summary: You have " + count + " active reservations scheduled for today (" + dateStr + ").",
                    "INFO"
            );
        }
    }



    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}