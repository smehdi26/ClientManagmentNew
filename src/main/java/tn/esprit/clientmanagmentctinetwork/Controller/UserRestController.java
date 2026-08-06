package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;
import tn.esprit.clientmanagmentctinetwork.Repository.AdminRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRestController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<AdminModel>> getAllUsers() {
        return ResponseEntity.ok(adminRepository.findAll());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<AdminModel> updateRole(@PathVariable Long id, @RequestParam("role") String role) {
        AdminModel user = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        return ResponseEntity.ok(adminRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}