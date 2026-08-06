package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "contract_history")
public class ContractHistoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year; // The completed calendar year

    @Column(columnDefinition = "TEXT")
    private String visitDates; // Comma-separated list of formatted dates

    @Column(nullable = false)
    private String redevance; // Active payment term during that year

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore // Prevent Jackson loops [1.2.4]
    private ContractModel contract;

    public ContractHistoryModel() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getVisitDates() { return visitDates; }
    public void setVisitDates(String visitDates) { this.visitDates = visitDates; }

    public String getRedevance() { return redevance; }
    public void setRedevance(String redevance) { this.redevance = redevance; }

    public ContractModel getContract() { return contract; }
    public void setContract(ContractModel contract) { this.contract = contract; }
}