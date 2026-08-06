package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
public class ContractModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String redevance; // ANNUELLE, SEMESTRIELLE, TRIMESTRIELLE

    @Column(nullable = false)
    private LocalDate dateSignature;

    @Column(nullable = false)
    private Integer numberOfVisits; // Automatically calculated (N.D.V)

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Physical Date Columns for Scheduled Visits [1.1.4]
    private LocalDate visitDate1;
    private LocalDate visitDate2;
    private LocalDate visitDate3;
    private LocalDate visitDate4;
    private LocalDate visitDate5;
    private LocalDate visitDate6;

    @ManyToOne(fetch = FetchType.EAGER) // Eagerly load client profile properties [1.2.6]
    @JoinColumn(name = "client_id", nullable = false)
    private ClientModel client;

    public ContractModel() {}

    // In-memory dynamic mapping translating exact dates to their French month names [1.1.4]
    @Transient
    public String getMonthsOfVisits() {
        java.util.List<String> months = new java.util.ArrayList<>();
        String[] frenchMonths = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        if (visitDate1 != null) months.add(frenchMonths[visitDate1.getMonthValue() - 1]);
        if (visitDate2 != null) months.add(frenchMonths[visitDate2.getMonthValue() - 1]);
        if (visitDate3 != null) months.add(frenchMonths[visitDate3.getMonthValue() - 1]);
        if (visitDate4 != null) months.add(frenchMonths[visitDate4.getMonthValue() - 1]);
        if (visitDate5 != null) months.add(frenchMonths[visitDate5.getMonthValue() - 1]);
        if (visitDate6 != null) months.add(frenchMonths[visitDate6.getMonthValue() - 1]);

        return String.join(", ", months);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRedevance() { return redevance; }
    public void setRedevance(String redevance) { this.redevance = redevance; }

    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }

    public Integer getNumberOfVisits() { return numberOfVisits; }
    public void setNumberOfVisits(Integer numberOfVisits) { this.numberOfVisits = numberOfVisits; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDate getVisitDate1() { return visitDate1; }
    public void setVisitDate1(LocalDate visitDate1) { this.visitDate1 = visitDate1; }

    public LocalDate getVisitDate2() { return visitDate2; }
    public void setVisitDate2(LocalDate visitDate2) { this.visitDate2 = visitDate2; }

    public LocalDate getVisitDate3() { return visitDate3; }
    public void setVisitDate3(LocalDate visitDate3) { this.visitDate3 = visitDate3; }

    public LocalDate getVisitDate4() { return visitDate4; }
    public void setVisitDate4(LocalDate visitDate4) { this.visitDate4 = visitDate4; }

    public LocalDate getVisitDate5() { return visitDate5; }
    public void setVisitDate5(LocalDate visitDate5) { this.visitDate5 = visitDate5; }

    public LocalDate getVisitDate6() { return visitDate6; }
    public void setVisitDate6(LocalDate visitDate6) { this.visitDate6 = visitDate6; }

    public ClientModel getClient() { return client; }
    public void setClient(ClientModel client) { this.client = client; }
}