package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.esprit.clientmanagmentctinetwork.Dto.AdminRegistrationDto;
import tn.esprit.clientmanagmentctinetwork.Model.AdminModel;
import tn.esprit.clientmanagmentctinetwork.Service.AdminService;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;

@Controller
public class AuthController {

    private final AdminService adminService;
    private final ClientService clientService;

    // Inject both services
    public AuthController(AdminService adminService, ClientService clientService) {
        this.adminService = adminService;
        this.clientService = clientService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("admin", new AdminRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerAdminAccount(
            @Valid @ModelAttribute("admin") AdminRegistrationDto registrationDto,
            BindingResult result) {

        AdminModel existing = adminService.findByEmail(registrationDto.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null) {
            if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", null, "Passwords do not match");
            }
        }

        if (result.hasErrors()) {
            return "register";
        }

        adminService.save(registrationDto);
        return "redirect:/register?success";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        java.util.List<tn.esprit.clientmanagmentctinetwork.Model.ClientModel> clients;

        if (keyword != null && !keyword.trim().isEmpty()) {
            clients = clientService.searchClients(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            clients = clientService.getAllClients();
        }

        // Calculate the sum of all active phones safely in Java
        long totalPhones = clients.stream()
                .filter(c -> c.getPhones() != null)
                .mapToLong(c -> c.getPhones().size())
                .sum();

        model.addAttribute("clients", clients);
        model.addAttribute("totalPhones", totalPhones); // Pass the pre-calculated sum
        return "dashboard";
    }
}