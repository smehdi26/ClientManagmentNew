package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ReservationRepository;

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

    public ReservationServiceImpl(ReservationRepository reservationRepository, ClientRepository clientRepository) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public ReservationModel createReservation(ReservationDto dto) {
        LocalDateTime bookingTime = LocalDateTime.of(dto.getDate(), dto.getTime());

        // Validate double-booking on active slots
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

        return reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long id) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation record not found."));
        reservation.setStatus("CANCELLED"); // Cancelling marks the slot free instantly
        reservationRepository.save(reservation);
    }

    @Override
    public List<TimeSlot> getSlotsForDate(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();

        // Sundays are closed
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return slots;
        }

        // Saturday (9 AM - 1 PM) vs Mon-Fri (9 AM - 5 PM)
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = (date.getDayOfWeek() == DayOfWeek.SATURDAY) ? LocalTime.of(13, 0) : LocalTime.of(17, 0);

        // Fetch confirmed reservations for the given date
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
        List<ReservationModel> dailyBookings = reservationRepository.findActiveByTimeRange(startOfDay, endOfDay);

        // Construct 30-minute intervals
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
    public void cancelReservation(Long id, String reason) {
        ReservationModel reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation record not found."));
        reservation.setStatus("CANCELLED");
        reservation.setCancellationReason(reason); // Store the reason
        reservationRepository.save(reservation);
    }

    @Override
    public void updateReservationStatus(Long id, String status) {
        ReservationModel r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        r.setStatus(status);
        r.setCancellationReason(null); // Clear reason if state changes back to active
        reservationRepository.save(r);
    }
}