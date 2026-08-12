package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    List<NotificationModel> findAllByOrderByCreatedAtDesc();
    long countByReadStatusFalse();

    boolean existsByReservationIdAndType(Long reservationId, String type);

    @Query("SELECT COUNT(n) > 0 FROM NotificationModel n WHERE n.type = 'INFO' AND n.message LIKE %:dateStr%")
    boolean existsDailySummaryForDate(@Param("dateStr") String dateStr);

    // ADD THIS: Checks if a trigger key is already in the database [1.1.2]
    boolean existsByTriggerKey(String triggerKey);

    // Finds notifications that are read AND older than a specific date
    void deleteByReadStatusTrueAndCreatedAtBefore(java.time.LocalDateTime threshold);
}