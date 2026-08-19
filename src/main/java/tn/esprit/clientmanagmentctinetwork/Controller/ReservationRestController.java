package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;
    private final UserRepository userRepository; // 2. Added Declaration

    public ReservationRestController(ReservationService reservationService, UserRepository userRepository) {
        this.reservationService = reservationService;
        this.userRepository = userRepository;
    }

    // GET all reservations with optional search and filter
    @GetMapping
    public ResponseEntity<List<ReservationModel>> getReservations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            @RequestParam(value = "priorityFilter", required = false) String priorityFilter) { // NEW

        List<ReservationModel> list = reservationService.searchAndFilterReservations(keyword, statusFilter, priorityFilter);
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
            @RequestParam(value = "reason", required = false) String reason,
            Authentication authentication) {

        String fullName = getAuthenticatedUserFullName(authentication);

        // Fix: Pass the 3rd argument here as well
        reservationService.cancelReservation(id, reason, fullName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper to get full name from database using the current authentication
     */
    private String getAuthenticatedUserFullName(Authentication authentication) {
        String email;
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }

        return userRepository.findByEmail(email)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown User");
    }

    // POST update reservation status manually
    @PostMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason,
            Authentication authentication) {

        // 1. Get the current user's full name
        String fullName = getAuthenticatedUserFullName(authentication);

        // 2. Call the service with the new 3-parameter signature
        if ("CANCELLED".equals(status)) {
            reservationService.cancelReservation(id, reason, fullName);
        } else {
            reservationService.updateReservationStatus(id, status, fullName);
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

    @PutMapping("/{id}")
    public ResponseEntity<ReservationModel> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationDto dto,
            Authentication authentication) {

        String fullName = getAuthenticatedUserFullName(authentication);
        return ResponseEntity.ok(reservationService.updateReservation(id, dto, fullName));
    }
}