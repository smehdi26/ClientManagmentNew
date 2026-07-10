package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationModel, Long> {

    @Query("SELECT r FROM ReservationModel r WHERE r.reservationTime = :time AND r.status != 'CANCELLED'")
    Optional<ReservationModel> findActiveByTime(@Param("time") LocalDateTime time);

    @Query("SELECT r FROM ReservationModel r WHERE r.reservationTime >= :start AND r.reservationTime < :end AND r.status != 'CANCELLED'")
    List<ReservationModel> findActiveByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<ReservationModel> findByClientIdOrderByReservationTimeDesc(Long clientId);
}