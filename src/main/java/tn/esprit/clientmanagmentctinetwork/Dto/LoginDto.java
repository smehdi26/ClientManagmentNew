package tn.esprit.clientmanagmentctinetwork.Dto;

public class LoginDto {
    private String username; // Maps to the client email
    private String password;

    // Constructors
    public LoginDto() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}