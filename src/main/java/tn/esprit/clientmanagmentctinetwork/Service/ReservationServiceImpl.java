package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ReservationRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository; // Added import

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
    private final NotificationRepository notificationRepository; // Added dependency

    // Inject all required dependencies
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ClientRepository clientRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository) { // Added parameter
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository; // Added mapping
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
        reservation.setClient(client);
        reservation.setReservationTime(bookingTime);
        reservation.setDescription(dto.getDescription());
        reservation.setStatus("UNTREATED");

        ReservationModel saved = reservationRepository.save(reservation);

        // Log action to the Notification Center
        notificationService.createNotification(
                "New reservation scheduled for client " + client.getName() + " on " + dto.getDate() + " at " + dto.getTime() + ".",
                "SUCCESS"
        );

        return saved;
    }

    // Overloaded method (1 parameter)
    @Override
    public void cancelReservation(Long id) {
        this.cancelReservation(id, null);
    }

    // Overloaded method (2 parameters)
    @Override
    public void cancelReservation(Long id, String reason) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation record not found."));
        reservation.setStatus("CANCELLED");
        reservation.setCancellationReason(reason);
        reservationRepository.save(reservation);

        // Log action to the Notification Center
        notificationService.createNotification(
                "Reservation for client " + reservation.getClient().getName() + " on " + reservation.getReservationTime().toLocalDate() + " has been CANCELLED. Reason: " + (reason != null && !reason.trim().isEmpty() ? reason : "Not specified"),
                "DANGER"
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
    public List<ReservationModel> getAllReservations() {
        return reservationRepository.findAllByOrderByReservationTimeDesc();
    }

    @Override
    public void updateReservationStatus(Long id, String status) {
        ReservationModel r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        r.setStatus(status);
        r.setCancellationReason(null);
        reservationRepository.save(r);

        // Log action to the Notification Center
        notificationService.createNotification(
                "Reservation status for client " + r.getClient().getName() + " updated manually to " + status + ".",
                "INFO"
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

        // Save reminders to database history if they do not already exist
        for (ReservationModel r : upcoming) {
            boolean alreadyLogged = notificationRepository.existsByReservationIdAndType(r.getId(), "WARNING");
            if (!alreadyLogged) {
                String clientPhone = r.getClient().getPhones().isEmpty() ? "No Number" : r.getClient().getPhones().get(0).getPhoneNumber();
                String message = "Reminder: Please contact " + r.getClient().getName() + " (" + clientPhone + ") for their upcoming appointment at " + r.getReservationTime().toLocalTime() + ".";

                NotificationModel notification = new NotificationModel();
                notification.setMessage(message);
                notification.setType("WARNING");
                notification.setReservationId(r.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notification.setReadStatus(false);
                notificationRepository.save(notification);
            }
        }
        return upcoming;
    }

    @Override
    public List<ReservationModel> searchAndFilterReservations(String keyword, String status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        if (cleanKeyword == null && cleanStatus == null) {
            return getAllReservations();
        }
        return reservationRepository.searchAndFilterReservations(cleanKeyword, cleanStatus);
    }
}