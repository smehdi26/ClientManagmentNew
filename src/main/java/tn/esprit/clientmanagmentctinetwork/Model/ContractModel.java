package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // Physical File Path & Original Name Columns for Visit Attachments [1.2.6]
    private String visitFile1;
    private String visitFileName1;
    private String visitFile2;
    private String visitFileName2;
    private String visitFile3;
    private String visitFileName3;
    private String visitFile4;
    private String visitFileName4;
    private String visitFile5;
    private String visitFileName5;
    private String visitFile6;
    private String visitFileName6;

    @ManyToOne(fetch = FetchType.EAGER) // Eagerly load client profile properties [1.2.6]
    @JoinColumn(name = "client_id", nullable = false)
    private ClientModel client;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("year DESC") // Orders history logs with newest year first [1.2.6]
    private List<ContractHistoryModel> history = new ArrayList<>();

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

    // Helper to format the unique file path with the backend download URL prefix [1.2.6]
    private String visitFileFile(String path) {
        return path != null ? "http://localhost:8090/api/contracts/files/" + path : null;
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

    // File getters/setters returning download link [1.2.6]
    public String getVisitFile1() { return visitFileFile(visitFile1); }
    public String getVisitFile1Raw() { return visitFile1; }
    public void setVisitFile1(String visitFile1) { this.visitFile1 = visitFile1; }
    public String getVisitFileName1() { return visitFileName1; }
    public void setVisitFileName1(String visitFileName1) { this.visitFileName1 = visitFileName1; }

    public String getVisitFile2() { return visitFileFile(visitFile2); }
    public String getVisitFile2Raw() { return visitFile2; }
    public void setVisitFile2(String visitFile2) { this.visitFile2 = visitFile2; }
    public String getVisitFileName2() { return visitFileName2; }
    public void setVisitFileName2(String visitFileName2) { this.visitFileName2 = visitFileName2; }

    public String getVisitFile3() { return visitFileFile(visitFile3); }
    public String getVisitFile3Raw() { return visitFile3; }
    public void setVisitFile3(String visitFile3) { this.visitFile3 = visitFile3; }
    public String getVisitFileName3() { return visitFileName3; }
    public void setVisitFileName3(String visitFileName3) { this.visitFileName3 = visitFileName3; }

    public String getVisitFile4() { return visitFileFile(visitFile4); }
    public String getVisitFile4Raw() { return visitFile4; }
    public void setVisitFile4(String visitFile4) { this.visitFile4 = visitFile4; }
    public String getVisitFileName4() { return visitFileName4; }
    public void setVisitFileName4(String visitFileName4) { this.visitFileName4 = visitFileName4; }

    public String getVisitFile5() { return visitFileFile(visitFile5); }
    public String getVisitFile5Raw() { return visitFile5; }
    public void setVisitFile5(String visitFile5) { this.visitFile5 = visitFile5; }
    public String getVisitFileName5() { return visitFileName5; }
    public void setVisitFileName5(String visitFileName5) { this.visitFileName5 = visitFileName5; }

    public String getVisitFile6() { return visitFileFile(visitFile6); }
    public String getVisitFile6Raw() { return visitFile6; }
    public void setVisitFile6(String visitFile6) { this.visitFile6 = visitFile6; }
    public String getVisitFileName6() { return visitFileName6; }
    public void setVisitFileName6(String visitFileName6) { this.visitFileName6 = visitFileName6; }

    public ClientModel getClient() { return client; }
    public void setClient(ClientModel client) { this.client = client; }

    public List<ContractHistoryModel> getHistory() { return history; }
    public void setHistory(List<ContractHistoryModel> history) { this.history = history; }
}