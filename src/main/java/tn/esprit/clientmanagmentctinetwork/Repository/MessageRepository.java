package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.MessageModel;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageModel, Long> {
    List<MessageModel> findByRecipientEmailOrderBySentAtDesc(String email);
    long countByRecipientEmailAndIsReadFalse(String email);
}