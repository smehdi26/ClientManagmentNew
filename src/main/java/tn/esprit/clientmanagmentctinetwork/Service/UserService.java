package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.AdminProfileDto;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;

public interface UserService {
    // Standard register action
    UserModel save(AdminRegistrationDto registrationDto);

    // Email lookup query
    UserModel findByEmail(String email);

    // Profile updates and password reset mapping [1.2.6]
    UserModel updateProfile(String existingEmail, AdminProfileDto dto);}