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
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService; // Added dependency

    // Constructor injecting all required dependencies
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           NotificationService notificationService) { // Added constructor parameter
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService; // Added mapping
    }

    @Override
    public UserModel save(AdminRegistrationDto registrationDto) {
        UserModel admin = new UserModel(
                registrationDto.getFirstName(),
                registrationDto.getLastName(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword()),
                registrationDto.getRole() // Map the role parameter
        );
        UserModel saved = userRepository.save(admin);

        // Extract clear role name for logging (e.g. "ROLE_HR" -> "HR")
        String roleName = saved.getRole().replace("ROLE_", "");

        // Log registration [1.2.6]
        notificationService.createNotification(
                "New " + roleName + " account '" + saved.getFirstName() + " " + saved.getLastName() + "' was successfully registered.",
                "SUCCESS",
                "USER"
        );

        return saved;
    }

    @Override
    public UserModel findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserModel updateProfile(String existingEmail, AdminProfileDto dto) {
        UserModel admin = userRepository.findByEmail(existingEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));

        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());

        if (!admin.getEmail().equalsIgnoreCase(dto.getEmail())) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                throw new IllegalStateException("Email address '" + dto.getEmail() + "' is already in use.");
            });
            admin.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        UserModel saved = userRepository.save(admin);

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
        UserModel user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    @Override
    public void processOAuthPostLogin(String email, String firstName, String lastName) {
        Optional<UserModel> existUser = userRepository.findByEmail(email);

        if (existUser.isEmpty()) {
            UserModel newUser = new UserModel();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole("ROLE_TECHNICIAN"); // Assign a default role
            newUser.setPassword(""); // OAuth users don't need a local password
            userRepository.save(newUser);

            notificationService.createNotification(
                    "New account created via Google: " + email, "SUCCESS", "USER"
            );
        }
    }
}