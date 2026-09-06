package tn.esprit.clientmanagmentctinetwork.Dto;

import java.time.LocalTime;

public class TimeSlot {
    private LocalTime time;
    private boolean booked;
    private String clientName;
    private Long reservationId;
    private String description;
    private String reservationName;

    public TimeSlot(LocalTime time, boolean booked, String clientName, Long reservationId, String description, String reservationName) {
        this.time = time;
        this.booked = booked;
        this.clientName = clientName;
        this.reservationId = reservationId;
        this.description = description;
        this.reservationName = reservationName;
    }

    // Getters and Setters
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReservationName() { return reservationName; }
    public void setReservationName(String reservationName) { this.reservationName = reservationName; }
}