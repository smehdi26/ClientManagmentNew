package tn.esprit.clientmanagmentctinetwork.Dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationDto {

    @NotNull(message = "Please select a client")
    private Long clientId;

    @jakarta.validation.constraints.NotBlank(message = "Reservation name/title is required")
    private String name;

    @NotNull(message = "Please select a booking date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull(message = "Please select a time slot")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

    private String description;

    private Long technicianId;

    // --- ADD THIS FIELD ---
    private String priority;


    // Getters and Setters
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    // --- ADD THESE METHODS ---
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}