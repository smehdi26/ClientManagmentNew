package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    Optional<ClientModel> findByEmail(String email);

    @Query("SELECT c FROM ClientModel c JOIN c.phones p WHERE p.phoneNumber = :phoneNumber")
    Optional<ClientModel> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Global Search Query
    @Query("SELECT DISTINCT c FROM ClientModel c LEFT JOIN c.phones p WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "p.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<ClientModel> searchClients(@Param("keyword") String keyword);
}