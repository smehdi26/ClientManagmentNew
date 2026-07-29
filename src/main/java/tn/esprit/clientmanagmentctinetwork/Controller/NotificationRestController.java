package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import tn.esprit.clientmanagmentctinetwork.Service.ContractService; // Added import
import tn.esprit.clientmanagmentctinetwork.Service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;
    private final ContractService contractService; // Added dependency

    // Constructor injecting both services
    public NotificationRestController(NotificationService notificationService,
                                      ContractService contractService) { // Added parameter
        this.notificationService = notificationService;
        this.contractService = contractService; // Added mapping
    }

    @GetMapping
    public ResponseEntity<List<NotificationModel>> getAllNotifications() {
        // 1. Evaluate any new maintenance contract warnings before returning the list
        contractService.checkContractNotifications();

        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}