package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.AdminProfileDto;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;

public interface AdminService {
    // Standard register action
    AdminModel save(AdminRegistrationDto registrationDto);

    // Email lookup query
    AdminModel findByEmail(String email);

    // Profile updates and password reset mapping [1.2.6]
    AdminModel updateProfile(String existingEmail, AdminProfileDto dto);}