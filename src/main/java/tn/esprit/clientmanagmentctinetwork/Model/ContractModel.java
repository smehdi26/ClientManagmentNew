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

    @Column(columnDefinition = "TEXT")
    private String monthsOfVisits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore // Prevent Jackson serialization loop [1.2.4]
    private ClientModel client;

    public ContractModel() {}

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

    public String getMonthsOfVisits() { return monthsOfVisits; }
    public void setMonthsOfVisits(String monthsOfVisits) { this.monthsOfVisits = monthsOfVisits; }

    public ClientModel getClient() { return client; }
    public void setClient(ClientModel client) { this.client = client; }
}