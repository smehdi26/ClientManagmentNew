package tn.esprit.clientmanagmentctinetwork.Dto;

import java.util.ArrayList;
import java.util.List;

public class ClientDto {
    private Long id;
    private String name;
    private String email;
    private String description;

    // Optional Extended Fields [1.2.6]
    private String address;
    private String city;
    private String contact; // Legal representative
    private String website;
    private Long sectorId; // Mapped sector ID

    private List<String> phones = new ArrayList<>();

    // Constructors
    public ClientDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }
}