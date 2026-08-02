package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminProfileDto;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;
import tn.esprit.clientmanagmentctinetwork.Repository.AdminRepository;

import java.util.Collections;

@Service
@Transactional
public class AdminServiceImpl implements AdminService, UserDetailsService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService; // Added dependency

    // Constructor injecting all required dependencies
    public AdminServiceImpl(AdminRepository adminRepository,
                            PasswordEncoder passwordEncoder,
                            NotificationService notificationService) { // Added constructor parameter
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService; // Added mapping
    }

    @Override
    public AdminModel save(AdminRegistrationDto registrationDto) {
        AdminModel admin = new AdminModel(
                registrationDto.getFirstName(),
                registrationDto.getLastName(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword())
        );
        AdminModel saved = adminRepository.save(admin);

        // LOG ACTION (USER CATEGORY) [1.2.6]
        notificationService.createNotification(
                "New administrator account '" + saved.getFirstName() + " " + saved.getLastName() + "' was successfully registered.",
                "SUCCESS",
                "USER"
        );

        return saved;
    }

    @Override
    public AdminModel findByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    @Override
    public AdminModel updateProfile(String existingEmail, AdminProfileDto dto) {
        AdminModel admin = adminRepository.findByEmail(existingEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));

        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());

        if (!admin.getEmail().equalsIgnoreCase(dto.getEmail())) {
            adminRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                throw new IllegalStateException("Email address '" + dto.getEmail() + "' is already in use.");
            });
            admin.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        AdminModel saved = adminRepository.save(admin);

        // LOG ACTION: Changed category from CLIENT to USER [1.2.6]
        notificationService.createNotification(
                "Administrator " + saved.getFirstName() + " " + saved.getLastName() + " updated their profile parameters.",
                "INFO",
                "USER"
        );

        return saved;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminModel admin = adminRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));

        return new User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(admin.getRole()))
        );
    }
}