package tn.esprit.clientmanagmentctinetwork.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // This ensures the test data is deleted (rolled back) after the test runs
public class UserServiceOAuthTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("TDD: Should create a new account when a Google user logs in for the first time")
    public void shouldCreateUserWhenGoogleEmailIsNew() {
        // 1. Arrange: Define a test email that doesn't exist in our DB
        String googleEmail = "new_social_user@gmail.com";
        String firstName = "Mehdi";
        String lastName = "GoogleTest";

        // Ensure the user doesn't already exist from a previous failed run
        userRepository.findByEmail(googleEmail).ifPresent(user -> userRepository.delete(user));

        // 2. Act: Call the service method that handles OAuth success
        userService.processOAuthPostLogin(googleEmail, firstName, lastName);

        // 3. Assert: Verify the user was created correctly
        Optional<UserModel> createdUser = userRepository.findByEmail(googleEmail);

        assertTrue(createdUser.isPresent(), "The user should have been saved to the database");
        assertEquals(googleEmail, createdUser.get().getEmail());
        assertEquals(firstName, createdUser.get().getFirstName());
        assertEquals("ROLE_TECHNICIAN", createdUser.get().getRole(), "Default social login role should be ROLE_TECHNICIAN");
    }

    @Test
    @DisplayName("TDD: Should not create a duplicate account if the email already exists")
    public void shouldNotCreateDuplicateUser() {
        // 1. Arrange: Manually create a user first
        String existingEmail = "existing@gmail.com";
        UserModel existing = new UserModel("Old", "User", existingEmail, "password", "ROLE_ADMIN");
        userRepository.save(existing);

        long initialCount = userRepository.count();

        // 2. Act: Try to process the same email via OAuth logic
        userService.processOAuthPostLogin(existingEmail, "New", "Name");

        // 3. Assert: Count should remain the same, data should not be overwritten
        assertEquals(initialCount, userRepository.count(), "User count should not increase");
        UserModel check = userRepository.findByEmail(existingEmail).get();
        assertEquals("Old", check.getFirstName(), "Existing user data should not be overwritten by OAuth login");
    }
}