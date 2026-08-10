package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean readStatus = false;

    @Column(nullable = false)
    private String type = "INFO"; // INFO, SUCCESS, DANGER

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "trigger_key", unique = true) // Database-level duplicate prevention [1.1.2]
    private String triggerKey;

    @Column(name = "client_phone")
    private String clientPhone; // Redirect reference to client profile

    @Column(nullable = false)
    private String category = "CLIENT"; // CLIENT, CONTRACT, RESERVATION

    private String title;
    private String statusLevel; // SAFE, REMINDER, URGENT, OVERDUE
    private String color;       // GREEN, YELLOW, RED
    private String priority;    // LOW, MEDIUM, HIGH, CRITICAL


    // Constructors
    public NotificationModel() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getTriggerKey() { return triggerKey; }
    public void setTriggerKey(String triggerKey) { this.triggerKey = triggerKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatusLevel() { return statusLevel; }
    public void setStatusLevel(String statusLevel) { this.statusLevel = statusLevel; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
}