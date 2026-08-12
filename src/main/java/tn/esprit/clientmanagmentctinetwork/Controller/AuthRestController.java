package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminProfileDto;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Dto.LoginDto;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;
import tn.esprit.clientmanagmentctinetwork.Service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    // 1. Declare the variables
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 2. Update the Constructor to inject them
    public AuthRestController(UserService userService,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // JSON Registration Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegistrationDto registrationDto) {
        // 3. Now 'userRepository' will be recognized!
        Optional<UserModel> existing = userRepository.findByEmail(registrationDto.getEmail());

        if (existing.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "There is already an account registered with that email");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        userService.save(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // JSON Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginDto loginDto) {
        UserModel admin = userService.findByEmail(loginDto.getUsername());

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
        UserModel admin = userService.findByEmail(email);
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
            UserModel updated = userService.updateProfile(existingEmail, dto);

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