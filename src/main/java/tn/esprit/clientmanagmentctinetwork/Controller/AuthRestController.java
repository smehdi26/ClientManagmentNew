package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminProfileDto;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.LoginDto;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;
import tn.esprit.clientmanagmentctinetwork.Service.AdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(AdminService adminService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    // JSON Registration Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegistrationDto registrationDto) {
        AdminModel existing = adminService.findByEmail(registrationDto.getEmail());
        if (existing != null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "There is already an account registered with that email");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // HTTP 409 Conflict
        }

        adminService.save(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // HTTP 201 Created
    }

    // JSON Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginDto loginDto) {
        AdminModel admin = adminService.findByEmail(loginDto.getUsername());

        // Match raw password with DB encrypted password
        if (admin == null || !passwordEncoder.matches(loginDto.getPassword(), admin.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // HTTP 401 Unauthorized
        }

        // Return user details payload on successful authentication
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", admin.getId());
        userDetails.put("email", admin.getEmail());
        userDetails.put("firstName", admin.getFirstName());
        userDetails.put("lastName", admin.getLastName());
        userDetails.put("role", admin.getRole());

        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("email") String email) {
        AdminModel admin = adminService.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam("existingEmail") String existingEmail,
            @Valid @RequestBody AdminProfileDto dto) { // Updated parameter
        try {
            AdminModel updated = adminService.updateProfile(existingEmail, dto);

            // Return updated user details payload
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", updated.getId());
            userDetails.put("email", updated.getEmail());
            userDetails.put("firstName", updated.getFirstName());
            userDetails.put("lastName", updated.getLastName());
            userDetails.put("role", updated.getRole());

            return ResponseEntity.ok(userDetails);
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
}