package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.ClientPhone;
import java.util.Optional;

@Repository
public interface ClientPhoneRepository extends JpaRepository<ClientPhone, Long> {
    Optional<ClientPhone> findByPhoneNumber(String phoneNumber);
}