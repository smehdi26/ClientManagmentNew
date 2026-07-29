package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public List<ClientPhone> getPhones() { return phones; }
    public void setPhones(List<ClientPhone> phones) { this.phones = phones; }

    // Null-safe helper to retrieve primary phone number
    public String getPrimaryPhoneNumber() {
        if (phones == null || phones.isEmpty()) {
            return "00000000"; // Fallback default
        }
        return phones.get(0).getPhoneNumber();
    }
}