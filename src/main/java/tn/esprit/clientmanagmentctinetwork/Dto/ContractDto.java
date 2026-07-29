package tn.esprit.clientmanagmentctinetwork.Dto;

import java.time.LocalDate;

public class ContractDto {
    private String name;
    private String redevance;
    private LocalDate dateSignature;
    private String monthsOfVisits;
    private Long clientId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRedevance() { return redevance; }
    public void setRedevance(String redevance) { this.redevance = redevance; }

    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }

    public String getMonthsOfVisits() { return monthsOfVisits; }
    public void setMonthsOfVisits(String monthsOfVisits) { this.monthsOfVisits = monthsOfVisits; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}