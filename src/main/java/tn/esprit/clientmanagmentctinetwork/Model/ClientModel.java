package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class ClientModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "client_code", nullable = false, unique = true)
    private String clientCode; // e.g., "CL0001" [1.1.2]

    private String address;
    private String city;
    private String contact; // Legal representative
    private String website;

    @ManyToOne(fetch = FetchType.EAGER) // Load sector object directly during client queries [1.2.6]
    @JoinColumn(name = "sector_id")
    private SectorModel sector; // Optional sector reference

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientPhone> phones = new ArrayList<>();

    // Helpers to manage bidirectional sync
    public void addPhone(ClientPhone phone) {
        phones.add(phone);
        phone.setClient(this);
    }

    public void removePhone(ClientPhone phone) {
        phones.remove(phone);
        phone.setClient(null);
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // This method runs automatically before the client is saved to the DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore // ADD THIS: Stops the serialization loop [1.2.4]
    private List<ContractModel> contracts = new java.util.ArrayList<>();

    public List<ContractModel> getContracts() { return contracts; }
    public void setContracts(List<ContractModel> contracts) { this.contracts = contracts; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClientCode() { return clientCode; }
    public void setClientCode(String clientCode) { this.clientCode = clientCode; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public SectorModel getSector() { return sector; }
    public void setSector(SectorModel sector) { this.sector = sector; }

    public List<ClientPhone> getPhones() { return phones; }
    public void setPhones(List<ClientPhone> phones) { this.phones = phones; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Null-safe helper to retrieve primary phone number
    public String getPrimaryPhoneNumber() {
        if (phones == null || phones.isEmpty()) {
            return "00000000"; // Fallback default
        }
        return phones.get(0).getPhoneNumber();
    }
}