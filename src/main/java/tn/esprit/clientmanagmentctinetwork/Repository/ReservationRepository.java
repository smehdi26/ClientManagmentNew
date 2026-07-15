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
    java.util.List<ReservationModel> findAllByOrderByReservationTimeDesc();

    // Counts active bookings for a specific day
    @Query("SELECT COUNT(r) FROM ReservationModel r WHERE r.status != 'CANCELLED' AND " +
            "r.reservationTime >= :start AND r.reservationTime <= :end")
    long countActiveByTimeRange(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    // Identifies bookings starting within 1 hour from now
    @Query("SELECT r FROM ReservationModel r WHERE r.status != 'CANCELLED' AND " +
            "r.reservationTime >= :now AND r.reservationTime <= :oneHourHence")
    List<ReservationModel> findUpcomingReservationsWithinHour(@Param("now") java.time.LocalDateTime now, @Param("oneHourHence") java.time.LocalDateTime oneHourHence);

    // Global search and status state filtering
    // 100% safe, timezone-neutral search and status state filtering
    @Query("SELECT DISTINCT r FROM ReservationModel r LEFT JOIN r.client c WHERE " +
            "(:status IS NULL OR :status = '' OR r.status = :status) AND (" +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(:searchHour IS NOT NULL AND HOUR(r.reservationTime) = :searchHour AND (:searchMinute IS NULL OR MINUTE(r.reservationTime) = :searchMinute)) OR " +
            "(:searchDateStart IS NOT NULL AND r.reservationTime >= :searchDateStart AND r.reservationTime <= :searchDateEnd)" +
            ") ORDER BY r.reservationTime DESC")
    List<ReservationModel> searchAndFilterReservations(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("searchHour") Integer searchHour,
            @Param("searchMinute") Integer searchMinute,
            @Param("searchDateStart") java.time.LocalDateTime searchDateStart,
            @Param("searchDateEnd") java.time.LocalDateTime searchDateEnd);
}