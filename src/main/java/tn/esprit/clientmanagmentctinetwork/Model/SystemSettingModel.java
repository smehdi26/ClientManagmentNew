package tn.esprit.clientmanagmentctinetwork.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "system_settings")
public class SystemSettingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "setting_key")
    private String key;

    @Column(nullable = false, name = "setting_value")
    private String value;

    public SystemSettingModel() {}

    public SystemSettingModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}