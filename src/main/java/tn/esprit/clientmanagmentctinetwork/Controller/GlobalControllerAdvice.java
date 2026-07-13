package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import tn.esprit.clientmanagmentctinetwork.Service.NotificationService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationsCount")
    public long getUnreadNotificationsCount() {
        return notificationService.getUnreadCount();
    }
}