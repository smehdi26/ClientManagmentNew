package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "client_phones")
public class ClientPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore // ADD THIS: Prevents Jackson infinite serialization loops
    private ClientModel client;

    public ClientPhone() {}

    public ClientPhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public ClientModel getClient() { return client; }
    public void setClient(ClientModel client) { this.client = client; }
}