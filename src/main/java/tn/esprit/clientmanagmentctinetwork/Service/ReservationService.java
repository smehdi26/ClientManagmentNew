package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationModel createReservation(ReservationDto dto);

    // Overloaded to support both versions and prevent compiler sync issues
    void cancelReservation(Long id);
    void cancelReservation(Long id, String reason);

    List<TimeSlot> getSlotsForDate(LocalDate date);
    List<ReservationModel> getReservationsByClientId(Long clientId);
    List<ReservationModel> getAllReservations();
    void updateReservationStatus(Long id, String status);
    long countTodayReservations();
    List<ReservationModel> getUpcomingAlerts();
    List<ReservationModel> searchAndFilterReservations(String keyword, String status);
    void deleteReservation(Long id);
}