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

    @Query("SELECT DISTINCT c FROM ContractModel c WHERE c.status = 'ACTIVE' AND (" +
            "(c.visitDate1 IS NOT NULL AND MONTH(c.visitDate1) = :month AND YEAR(c.visitDate1) = :year) OR " +
            "(c.visitDate2 IS NOT NULL AND MONTH(c.visitDate2) = :month AND YEAR(c.visitDate2) = :year) OR " +
            "(c.visitDate3 IS NOT NULL AND MONTH(c.visitDate3) = :month AND YEAR(c.visitDate3) = :year) OR " +
            "(c.visitDate4 IS NOT NULL AND MONTH(c.visitDate4) = :month AND YEAR(c.visitDate4) = :year) OR " +
            "(c.visitDate5 IS NOT NULL AND MONTH(c.visitDate5) = :month AND YEAR(c.visitDate5) = :year) OR " +
            "(c.visitDate6 IS NOT NULL AND MONTH(c.visitDate6) = :month AND YEAR(c.visitDate6) = :year)" +
            ")")
    List<ContractModel> findByVisitMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Dynamic search and status state filtering [1.2.1]
    @Query("SELECT DISTINCT c FROM ContractModel c LEFT JOIN c.client cl WHERE " +
            "(:redevance IS NULL OR :redevance = '' OR c.redevance = :redevance) AND " +
            "(:status IS NULL OR :status = '' OR c.status = :status) AND (" + // Added status check
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.dateSignature, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ") ORDER BY c.dateSignature DESC")
    List<ContractModel> searchAndFilterContracts(
            @Param("keyword") String keyword,
            @Param("redevance") String redevance,
            @Param("status") String status); // Added parameter
}