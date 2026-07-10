package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;

public interface AdminService {
    AdminModel save(AdminRegistrationDto registrationDto);
    AdminModel findByEmail(String email);
}