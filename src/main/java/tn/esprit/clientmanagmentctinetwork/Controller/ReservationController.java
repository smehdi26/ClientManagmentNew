package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.time.LocalDate;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ClientService clientService;

    public ReservationController(ReservationService reservationService, ClientService clientService) {
        this.reservationService = reservationService;
        this.clientService = clientService;
    }

    @GetMapping
    public String showScheduler(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "clientId", required = false) Long clientId, // New Parameter
            Model model) {

        LocalDate date = (dateStr == null || dateStr.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);

        model.addAttribute("selectedDate", date);
        model.addAttribute("slots", reservationService.getSlotsForDate(date));
        model.addAttribute("clients", clientService.getAllClients());

        ReservationDto dto = new ReservationDto();
        dto.setDate(date);

        // Auto-select client if ID is provided
        if (clientId != null) {
            dto.setClientId(clientId);
        }
        model.addAttribute("reservation", dto);

        return "reservations";
    }

    @PostMapping("/book")
    public String bookSlot(
            @Valid @ModelAttribute("reservation") ReservationDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("selectedDate", dto.getDate());
            model.addAttribute("slots", reservationService.getSlotsForDate(dto.getDate()));
            model.addAttribute("clients", clientService.getAllClients());
            return "reservations";
        }

        try {
            reservationService.createReservation(dto);
        } catch (IllegalStateException e) {
            result.rejectValue("time", null, e.getMessage());
            model.addAttribute("selectedDate", dto.getDate());
            model.addAttribute("slots", reservationService.getSlotsForDate(dto.getDate()));
            model.addAttribute("clients", clientService.getAllClients());
            return "reservations";
        }

        return "redirect:/reservations?date=" + dto.getDate();
    }

    // Flexible cancellation route that redirects dynamically
    @PostMapping("/cancel/{id}")
    public String cancelSlot(
            @PathVariable("id") Long id,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "redirect", required = false) String redirectUrl) {

        reservationService.cancelReservation(id);

        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/reservations?date=" + dateStr;
    }
}