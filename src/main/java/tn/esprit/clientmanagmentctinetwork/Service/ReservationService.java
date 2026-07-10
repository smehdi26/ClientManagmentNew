package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.TimeSlot;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationModel createReservation(ReservationDto dto);
    void cancelReservation(Long id);
    List<TimeSlot> getSlotsForDate(LocalDate date);
    List<ReservationModel> getReservationsByClientId(Long clientId);
    void cancelReservation(Long id, String reason);
    void updateReservationStatus(Long id, String status);
    List<ReservationModel> getAllReservations();
}