package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ReservationRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ClientRepository clientRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReservationModel> getAllReservations() {
        return reservationRepository.findAllByOrderByReservationTimeDesc();
    }

    @Override
    public List<ReservationModel> searchAndFilterReservations(String keyword, String status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Integer searchHour = null;
        Integer searchMinute = null;
        LocalDateTime searchDateStart = null;
        LocalDateTime searchDateEnd = null;

        if (cleanKeyword != null) {
            if (cleanKeyword.matches("^\\d{1,2}:\\d{2}$")) {
                String[] parts = cleanKeyword.split(":");
                searchHour = Integer.parseInt(parts[0]);
                searchMinute = Integer.parseInt(parts[1]);
            }
            else if (cleanKeyword.matches("^\\d{1,2}$")) {
                int value = Integer.parseInt(cleanKeyword);
                if (value >= 0 && value <= 23) {
                    searchHour = value;
                }
            }
            try {
                LocalDate parsedDate = LocalDate.parse(cleanKeyword);
                searchDateStart = LocalDateTime.of(parsedDate, LocalTime.MIN);
                searchDateEnd = LocalDateTime.of(parsedDate, LocalTime.MAX);
            } catch (Exception ignored) {}

            if (searchHour != null) {
                java.time.ZoneOffset offset = java.time.ZoneId.systemDefault().getRules().getOffset(java.time.Instant.now());
                int offsetHours = offset.getTotalSeconds() / 3600;
                searchHour = searchHour - offsetHours;
                if (searchHour < 0) searchHour += 24;
                else if (searchHour > 23) searchHour -= 24;
            }
        }

        if (cleanKeyword == null && cleanStatus == null) {
            return getAllReservations();
        }

        return reservationRepository.searchAndFilterReservations(
                cleanKeyword, cleanStatus, searchHour, searchMinute, searchDateStart, searchDateEnd
        );
    }

    @Override
    public ReservationModel createReservation(ReservationDto dto) {
        LocalDateTime bookingTime = LocalDateTime.of(dto.getDate(), dto.getTime());

        Optional<ReservationModel> active = reservationRepository.findActiveByTime(bookingTime);
        if (active.isPresent()) {
            throw new IllegalStateException("This time slot has already been booked by another client.");
        }

        ClientModel client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ReservationModel reservation = new ReservationModel();
        reservation.setName(dto.getName());
        reservation.setClient(client);
        reservation.setReservationTime(bookingTime);
        reservation.setDescription(dto.getDescription());
        reservation.setStatus("UNTREATED");

        // UPDATED: Map Priority Field (Default to MEDIUM if null)
        String priority = (dto.getPriority() != null) ? dto.getPriority().toUpperCase() : "MEDIUM";
        reservation.setPriority(priority);

        if (dto.getTechnicianId() != null) {
            UserModel technician = userRepository.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new IllegalArgumentException("IT Technician not found"));
            reservation.setTechnician(technician);
        }

        ReservationModel saved = reservationRepository.save(reservation);
        String primaryPhone = client.getPrimaryPhoneNumber();

        // UPDATED: Logic to determine Notification styling based on Priority
        String color = "GREEN";
        String statusLevel = "SAFE";
        if ("HIGH".equals(priority)) { color = "YELLOW"; statusLevel = "REMINDER"; }
        if ("CRITICAL".equals(priority)) { color = "RED"; statusLevel = "URGENT"; }

        // LOG ACTION WITH DYNAMIC PRIORITY LEVELS
        notificationService.createDetailedNotification(
                "Nouvelle réservation",
                "New reservation '" + saved.getName() + "' (" + priority + ") scheduled for client " + client.getName() + " on " + dto.getDate() + ".",
                "SUCCESS", "RESERVATION", statusLevel, color, priority,
                "RESERVATION_CREATION_" + saved.getId(), null, primaryPhone
        );

        return saved;
    }

    @Override
    public void cancelReservation(Long id, String reason) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation record not found."));
        reservation.setStatus("CANCELLED");
        reservation.setCancellationReason(reason);
        reservationRepository.save(reservation);

        String primaryPhone = reservation.getClient() != null ? reservation.getClient().getPrimaryPhoneNumber() : null;

        notificationService.createDetailedNotification(
                "Réservation annulée",
                "Reservation for client " + reservation.getClient().getName() + " has been CANCELLED. Reason: " + (reason != null ? reason : "Not specified"),
                "DANGER", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_CANCEL_" + reservation.getId(), null, primaryPhone
        );
    }

    @Override
    public List<TimeSlot> getSlotsForDate(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) return slots;

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = (date.getDayOfWeek() == DayOfWeek.SATURDAY) ? LocalTime.of(13, 0) : LocalTime.of(17, 0);

        List<ReservationModel> dailyBookings = reservationRepository.findActiveByTimeRange(
                LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

        LocalTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalTime finalCurrent = current;
            Optional<ReservationModel> match = dailyBookings.stream()
                    .filter(r -> r.getReservationTime().toLocalTime().equals(finalCurrent))
                    .findFirst();

            if (match.isPresent()) {
                ReservationModel r = match.get();
                slots.add(new TimeSlot(finalCurrent, true, r.getClient().getName(), r.getId(), r.getDescription()));
            } else {
                slots.add(new TimeSlot(finalCurrent, false, null, null, null));
            }
            current = current.plusMinutes(30);
        }
        return slots;
    }

    @Override
    public void updateReservationStatus(Long id, String status) {
        ReservationModel r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        r.setStatus(status);
        r.setCancellationReason(null);
        reservationRepository.save(r);

        String primaryPhone = r.getClient() != null ? r.getClient().getPrimaryPhoneNumber() : null;

        notificationService.createDetailedNotification(
                "Statut de réservation modifié",
                "Reservation status for " + r.getClient().getName() + " updated to " + status + ".",
                "INFO", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_STATUS_" + r.getId() + "_" + System.currentTimeMillis(), null, primaryPhone
        );
    }

    @Override
    public List<ReservationModel> getUpcomingAlerts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourHence = now.plusHours(1);
        List<ReservationModel> upcoming = reservationRepository.findUpcomingReservationsWithinHour(now, oneHourHence);

        for (ReservationModel r : upcoming) {
            boolean alreadyLogged = notificationRepository.existsByReservationIdAndType(r.getId(), "WARNING");
            if (!alreadyLogged) {
                String phone = r.getClient().getPrimaryPhoneNumber();
                String message = "Reminder: Contact " + r.getClient().getName() + " (" + phone + ") for meeting at " + r.getReservationTime().toLocalTime() + ".";

                notificationService.createDetailedNotification(
                        "Rappel de réunion",
                        message,
                        "WARNING", "RESERVATION", "REMINDER", "YELLOW", "MEDIUM",
                        "RESERVATION_WARNING_" + r.getId(), null, phone
                );
            }
        }
        return upcoming;
    }

    @Override
    public void deleteReservation(Long id) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        reservationRepository.delete(reservation);

        notificationService.createDetailedNotification(
                "Réservation supprimée",
                "Reservation '" + reservation.getName() + "' has been permanently deleted.",
                "DANGER", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_DELETE_" + reservation.getId() + "_" + System.currentTimeMillis(), null, null
        );
    }

    @Override
    public void cancelReservation(Long id) { cancelReservation(id, null); }

    @Override
    public List<ReservationModel> getReservationsByClientId(Long clientId) {
        return reservationRepository.findByClientIdOrderByReservationTimeDesc(clientId);
    }

    @Override
    public long countTodayReservations() {
        return reservationRepository.countActiveByTimeRange(
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN),
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
    }
}