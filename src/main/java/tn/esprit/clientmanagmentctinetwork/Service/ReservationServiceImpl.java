package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ReservationRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.AdminRepository;

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
    private final AdminRepository adminRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ClientRepository clientRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  AdminRepository adminRepository) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public List<ReservationModel> getAllReservations() {
        return reservationRepository.findAllByOrderByReservationTimeDesc();
    }

    // Restores the timezone-safe search and filter implementation [1.2.1, 1.2.6]
    @Override
    public List<ReservationModel> searchAndFilterReservations(String keyword, String status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Integer searchHour = null;
        Integer searchMinute = null;
        LocalDateTime searchDateStart = null;
        LocalDateTime searchDateEnd = null;

        if (cleanKeyword != null) {
            // 1. Try parsing as exact hour and minute (e.g., "09:30")
            if (cleanKeyword.matches("^\\d{1,2}:\\d{2}$")) {
                String[] parts = cleanKeyword.split(":");
                searchHour = Integer.parseInt(parts[0]);
                searchMinute = Integer.parseInt(parts[1]);
            }
            // 2. Try parsing as a standalone hour (e.g., "09" or "9")
            else if (cleanKeyword.matches("^\\d{1,2}$")) {
                int value = Integer.parseInt(cleanKeyword);
                if (value >= 0 && value <= 23) {
                    searchHour = value;
                }
            }
            // 3. Try parsing as a LocalDate object and calculate safe boundaries in Java [1.1.3]
            try {
                LocalDate parsedDate = LocalDate.parse(cleanKeyword);
                searchDateStart = LocalDateTime.of(parsedDate, LocalTime.MIN); // Start of day
                searchDateEnd = LocalDateTime.of(parsedDate, LocalTime.MAX);   // End of day
            } catch (Exception e) {
                // Ignore if not a valid date format
            }

            // 4. Dynamically adjust search hour to match the database's timezone (UTC) [1.2.1]
            if (searchHour != null) {
                java.time.ZoneOffset offset = java.time.ZoneId.systemDefault().getRules().getOffset(java.time.Instant.now());
                int offsetHours = offset.getTotalSeconds() / 3600; // e.g. +1 for WAT / Tunis

                searchHour = searchHour - offsetHours;

                if (searchHour < 0) {
                    searchHour += 24;
                } else if (searchHour > 23) {
                    searchHour -= 24;
                }
            }
        }

        if (cleanKeyword == null && cleanStatus == null) {
            return getAllReservations();
        }

        return reservationRepository.searchAndFilterReservations(
                cleanKeyword,
                cleanStatus,
                searchHour,
                searchMinute,
                searchDateStart,
                searchDateEnd
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

        if (dto.getTechnicianId() != null) {
            AdminModel technician = adminRepository.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new IllegalArgumentException("IT Technician not found"));
            reservation.setTechnician(technician);
        }

        ReservationModel saved = reservationRepository.save(reservation);
        String primaryPhone = client.getPrimaryPhoneNumber();

        // LOG ACTION WITH CLIENT PHONE & PRIORITY DETAILED PROPERTIES [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Nouvelle réservation",
                "New reservation '" + saved.getName() + "' scheduled for client " + client.getName() + " on " + dto.getDate() + " at " + dto.getTime() + ".",
                "SUCCESS", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_CREATION_" + saved.getId(), null, primaryPhone
        );

        return saved;
    }

    @Override
    public void cancelReservation(Long id) {
        this.cancelReservation(id, null);
    }

    @Override
    public void cancelReservation(Long id, String reason) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation record not found."));
        reservation.setStatus("CANCELLED");
        reservation.setCancellationReason(reason);
        reservationRepository.save(reservation);

        String primaryPhone = reservation.getClient() != null ? reservation.getClient().getPrimaryPhoneNumber() : null;

        // LOG ACTION WITH CLIENT PHONE & PRIORITY DETAILED PROPERTIES [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Réservation annulée",
                "Reservation for client " + reservation.getClient().getName() + " on " + reservation.getReservationTime().toLocalDate() + " has been CANCELLED. Reason: " + (reason != null && !reason.trim().isEmpty() ? reason : "Not specified"),
                "DANGER", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_CANCEL_" + reservation.getId(), null, primaryPhone
        );
    }

    @Override
    public List<TimeSlot> getSlotsForDate(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return slots;
        }

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = (date.getDayOfWeek() == DayOfWeek.SATURDAY) ? LocalTime.of(13, 0) : LocalTime.of(17, 0);

        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
        List<ReservationModel> dailyBookings = reservationRepository.findActiveByTimeRange(startOfDay, endOfDay);

        LocalTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalTime slotTime = current;
            Optional<ReservationModel> match = dailyBookings.stream()
                    .filter(r -> r.getReservationTime().toLocalTime().equals(slotTime))
                    .findFirst();

            if (match.isPresent()) {
                ReservationModel r = match.get();
                slots.add(new TimeSlot(slotTime, true, r.getClient().getName(), r.getId(), r.getDescription()));
            } else {
                slots.add(new TimeSlot(slotTime, false, null, null, null));
            }
            current = current.plusMinutes(30);
        }
        return slots;
    }

    @Override
    public List<ReservationModel> getReservationsByClientId(Long clientId) {
        return reservationRepository.findByClientIdOrderByReservationTimeDesc(clientId);
    }

    @Override
    public void updateReservationStatus(Long id, String status) {
        ReservationModel r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        r.setStatus(status);
        r.setCancellationReason(null);
        reservationRepository.save(r);

        String primaryPhone = r.getClient() != null ? r.getClient().getPrimaryPhoneNumber() : null;

        // LOG ACTION WITH CLIENT PHONE & PRIORITY DETAILED PROPERTIES [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Statut de réservation modifié",
                "Reservation status for client " + r.getClient().getName() + " updated manually to " + status + ".",
                "INFO", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_STATUS_" + r.getId() + "_" + System.currentTimeMillis(), null, primaryPhone
        );
    }

    @Override
    public long countTodayReservations() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        return reservationRepository.countActiveByTimeRange(start, end);
    }

    @Override
    public List<ReservationModel> getUpcomingAlerts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourHence = now.plusHours(1);
        List<ReservationModel> upcoming = reservationRepository.findUpcomingReservationsWithinHour(now, oneHourHence);

        for (ReservationModel r : upcoming) {
            boolean alreadyLogged = notificationRepository.existsByReservationIdAndType(r.getId(), "WARNING");
            if (!alreadyLogged) {
                String clientPhone = r.getClient().getPhones().isEmpty() ? "No Number" : r.getClient().getPhones().get(0).getPhoneNumber();
                String message = "Reminder: Please contact " + r.getClient().getName() + " (" + clientPhone + ") for their upcoming appointment at " + r.getReservationTime().toLocalTime() + ".";

                // LOG ACTION WITH CLIENT PHONE & PRIORITY DETAILED PROPERTIES [1.2.1, 1.2.6]
                notificationService.createDetailedNotification(
                        "Rappel de réunion",
                        message,
                        "WARNING", "RESERVATION", "REMINDER", "YELLOW", "MEDIUM",
                        "RESERVATION_WARNING_" + r.getId(), null, clientPhone
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

        String primaryPhone = reservation.getClient() != null ? reservation.getClient().getPrimaryPhoneNumber() : null;

        // LOG ACTION WITH CLIENT PHONE & PRIORITY DETAILED PROPERTIES [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Réservation supprimée",
                "Reservation/Meeting '" + reservation.getName() + "' has been permanently deleted.",
                "DANGER", "RESERVATION", "SAFE", "GREEN", "LOW",
                "RESERVATION_DELETE_" + reservation.getId() + "_" + System.currentTimeMillis(), null, primaryPhone
        );
    }
}