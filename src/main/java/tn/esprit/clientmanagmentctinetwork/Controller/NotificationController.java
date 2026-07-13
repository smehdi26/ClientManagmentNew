package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tn.esprit.clientmanagmentctinetwork.Service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String showNotifications(Model model) {
        model.addAttribute("notifications", notificationService.getAllNotifications());
        return "notification-list";
    }

    @PostMapping("/read-all")
    public String markAllAsRead() {
        notificationService.markAllAsRead();
        return "redirect:/notifications";
    }
}