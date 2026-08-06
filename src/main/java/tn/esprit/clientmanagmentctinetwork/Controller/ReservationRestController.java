package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET all reservations with optional search and filter
    @GetMapping
    public ResponseEntity<List<ReservationModel>> getReservations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String statusFilter) {

        List<ReservationModel> list = reservationService.searchAndFilterReservations(keyword, statusFilter);
        return ResponseEntity.ok(list);
    }

    // GET 30-minute intervals for a specific date
    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlot>> getSlots(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlot> slots = reservationService.getSlotsForDate(date);
        return ResponseEntity.ok(slots);
    }

    // POST book a slot
    @PostMapping("/book")
    public ResponseEntity<ReservationModel> bookSlot(@RequestBody ReservationDto dto) {
        ReservationModel saved = reservationService.createReservation(dto);
        return ResponseEntity.ok(saved);
    }

    // POST cancel a slot with optional reason
    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelSlot(
            @PathVariable Long id,
            @RequestParam(value = "reason", required = false) String reason) {

        reservationService.cancelReservation(id, reason);
        return ResponseEntity.noContent().build();
    }

    // POST update reservation status manually
    @PostMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason) {

        if ("CANCELLED".equals(status)) {
            reservationService.cancelReservation(id, reason);
        } else {
            reservationService.updateReservationStatus(id, status);
        }
        return ResponseEntity.noContent().build();
    }

    // GET count of today's active bookings
    @GetMapping("/today-count")
    public ResponseEntity<Long> getTodayCount() {
        return ResponseEntity.ok(reservationService.countTodayReservations());
    }

    // GET list of active bookings starting within 1 hour
    @GetMapping("/upcoming-alerts")
    public ResponseEntity<List<ReservationModel>> getUpcomingAlerts() {
        return ResponseEntity.ok(reservationService.getUpcomingAlerts());
    }

    // DELETE: Permanently delete/cancel reservation [1.2.6]
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}