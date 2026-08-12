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
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Service.UserService;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;
import tn.esprit.clientmanagmentctinetwork.Service.ContractService; // Added import
import tn.esprit.clientmanagmentctinetwork.Service.NotificationService;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final ClientService clientService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final ContractService contractService; // Added dependency

    // Constructor injecting all required services
    public AuthController(UserService userService,
                          ClientService clientService,
                          ReservationService reservationService,
                          NotificationService notificationService,
                          ContractService contractService) { // Added parameter
        this.userService = userService;
        this.clientService = clientService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.contractService = contractService; // Added mapping
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

        UserModel existing = userService.findByEmail(registrationDto.getEmail());
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

        userService.save(registrationDto);
        return "redirect:/register?success";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // 1. Run maintenance contract periodic checks on page entry
        contractService.checkContractNotifications();

        List<ClientModel> clients;
        if (keyword != null && !keyword.trim().isEmpty()) {
            clients = clientService.searchClients(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            clients = clientService.getAllClients();
        }

        // Calculate the sum of all registered phones safely on the Java side
        long totalPhones = clients.stream()
                .filter(c -> c.getPhones() != null)
                .mapToLong(c -> c.getPhones().size())
                .sum();

        model.addAttribute("clients", clients);
        model.addAttribute("totalPhones", totalPhones);

        // Fetch Notification Systems
        long todayCount = 0;
        if (clients.size() > 0) {
            todayCount = reservationService.countTodayReservations();
            if (todayCount > 0) {
                // Log daily overview popup state to DB history if it is the first load of the day
                notificationService.logDailySummaryIfNew(todayCount);
            }
        }
        model.addAttribute("todayReservationsCount", todayCount);
        model.addAttribute("upcomingAlerts", reservationService.getUpcomingAlerts());

        return "dashboard";
    }
}