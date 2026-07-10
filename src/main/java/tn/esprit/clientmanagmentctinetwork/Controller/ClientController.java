package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ClientDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.util.Optional;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;
    private final ReservationService reservationService; // Inject ReservationService

    public ClientController(ClientService clientService, ReservationService reservationService) {
        this.clientService = clientService;
        this.reservationService = reservationService;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        ClientDto clientDto = new ClientDto();
        clientDto.getPhones().add("");
        model.addAttribute("client", clientDto);
        return "client-add";
    }

    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute("client") ClientDto clientDto, BindingResult result) {
        validatePhones(clientDto, result, null);

        if (result.hasErrors()) {
            return "client-add";
        }

        clientService.saveClient(clientDto);
        return "redirect:/dashboard";
    }

    // Updated viewClient mapping to load reservation history
    @GetMapping("/{phoneNumber}")
    public String viewClient(@PathVariable("phoneNumber") String phoneNumber, Model model) {
        Optional<ClientModel> clientOpt = clientService.findByPhoneNumber(phoneNumber);
        if (clientOpt.isEmpty()) {
            return "redirect:/dashboard";
        }
        ClientModel client = clientOpt.get();
        model.addAttribute("client", client);
        model.addAttribute("reservations", reservationService.getReservationsByClientId(client.getId()));
        return "client-view";
    }

    @GetMapping("/{phoneNumber}/edit")
    public String showEditForm(@PathVariable("phoneNumber") String phoneNumber, Model model) {
        Optional<ClientModel> client = clientService.findByPhoneNumber(phoneNumber);
        if (client.isEmpty()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("client", clientService.convertToDto(client.get()));
        model.addAttribute("existingPhone", phoneNumber);
        return "client-edit";
    }

    @PostMapping("/{existingPhone}/edit")
    public String editClient(
            @PathVariable("existingPhone") String existingPhone,
            @Valid @ModelAttribute("client") ClientDto clientDto,
            BindingResult result,
            Model model) {

        validatePhones(clientDto, result, clientDto.getId());

        if (result.hasErrors()) {
            model.addAttribute("existingPhone", existingPhone);
            return "client-edit";
        }

        clientService.updateClient(existingPhone, clientDto);
        return "redirect:/dashboard";
    }

    @PostMapping("/{phoneNumber}/delete")
    public String deleteClient(@PathVariable("phoneNumber") String phoneNumber) {
        clientService.deleteClientByPhoneNumber(phoneNumber);
        return "redirect:/dashboard";
    }

    private void validatePhones(ClientDto clientDto, BindingResult result, Long clientId) {
        if (clientDto.getPhones() == null || clientDto.getPhones().stream().allMatch(String::isEmpty)) {
            result.rejectValue("phones", null, "At least one phone number is required");
            return;
        }

        for (String phone : clientDto.getPhones()) {
            if (phone != null && !phone.trim().isEmpty()) {
                String cleanPhone = phone.trim();
                if (!cleanPhone.matches("^[0-9]{8}$")) {
                    result.rejectValue("phones", null, "Phone number '" + cleanPhone + "' must be exactly 8 digits.");
                } else if (!clientService.isPhoneUniqueExcludingClient(cleanPhone, clientId)) {
                    result.rejectValue("phones", null, "Phone number '" + cleanPhone + "' is already assigned to another client.");
                }
            }
        }
    }
}