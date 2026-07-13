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

    // Checks if warning has already been logged for a reservation
    boolean existsByReservationIdAndType(Long reservationId, String type);

    // Checks if a daily summary was already logged for today
    @Query("SELECT COUNT(n) > 0 FROM NotificationModel n WHERE n.type = 'INFO' AND n.message LIKE %:dateStr%")
    boolean existsDailySummaryForDate(@Param("dateStr") String dateStr);
}