package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<ContractModel, Long> {
    List<ContractModel> findByClientId(Long clientId);

    // Global search and filter query
    @Query("SELECT DISTINCT c FROM ContractModel c LEFT JOIN c.client cl WHERE " +
            "(:redevance IS NULL OR :redevance = '' OR c.redevance = :redevance) AND (" +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.monthsOfVisits) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.dateSignature, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ") ORDER BY c.dateSignature DESC")
    List<ContractModel> searchAndFilterContracts(
            @Param("keyword") String keyword,
            @Param("redevance") String redevance);
}