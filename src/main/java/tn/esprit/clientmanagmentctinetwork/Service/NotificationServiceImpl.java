package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.scheduling.annotation.Scheduled;
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
        // Updated to use the master detailed mapper with default null redirection parameters
        this.createDetailedNotification(
                "System Event", message, type, category,
                "SAFE", "GREEN", "LOW",
                System.currentTimeMillis() + "_" + message.hashCode(), null, null
        );
    }

    // Overload for backward compatibility
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
            this.createDetailedNotification(
                    "Daily Agenda Summary",
                    "Daily Agenda Summary: You have " + count + " active reservations scheduled for today (" + dateStr + ").",
                    "INFO", "CLIENT", "SAFE", "GREEN", "LOW",
                    "DAILY_SUMMARY_" + dateStr, null, null
            );
        }
    }

    // Master detailed logger containing exact database checks to prevent duplicate alerts [1.1.2, 1.2.6]
    @Override
    public void createDetailedNotification(String title, String message, String type, String category,
                                           String statusLevel, String color, String priority,
                                           String triggerKey, Long contractId, String clientPhone) {
        boolean exists = notificationRepository.existsByTriggerKey(triggerKey);
        if (!exists) {
            NotificationModel notification = new NotificationModel();
            notification.setTriggerKey(triggerKey);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setCategory(category);
            notification.setStatusLevel(statusLevel);
            notification.setColor(color);
            notification.setPriority(priority);
            notification.setContractId(contractId);
            notification.setClientPhone(clientPhone); // Store client phone redirection reference
            notification.setCreatedAt(LocalDateTime.now());
            notification.setReadStatus(false);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
            System.out.println("Notification " + id + " set to READ in database.");
        });
    }

    // Runs every day at midnight
    /*@Scheduled(cron = "0 0 0 * * *")
    public void autoDeleteOldNotifications() {
        // Calculate the date 45 days ago
        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minusDays(45);

        // Custom query in repository or just logic here
        List<NotificationModel> oldNotifications = notificationRepository.findAll()
                .stream()
                .filter(n -> n.isReadStatus() && n.getCreatedAt().isBefore(threshold))
                .toList();

        notificationRepository.deleteAll(oldNotifications);
        System.out.println("Cleanup: Deleted " + oldNotifications.size() + " old read notifications.");
    }*/
}