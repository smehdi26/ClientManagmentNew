package tn.esprit.clientmanagmentctinetwork.Controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ReservationDto;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ClientService clientService;

    public ReservationController(ReservationService reservationService, ClientService clientService) {
        this.reservationService = reservationService;
        this.clientService = clientService;
    }

    // 1. Directory View: List all reservations with pre-calculated counts
    @GetMapping
    public String showAllReservations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            Model model) {

        List<ReservationModel> reservations =
                reservationService.searchAndFilterReservations(keyword, statusFilter);

        model.addAttribute("reservations", reservations);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);

        // Calculate the status counts in Java (prevents Thymeleaf SpEL selection crashes)
        long untreated = reservations.stream().filter(r -> "UNTREATED".equals(r.getStatus())).count();
        long inprogress = reservations.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count();
        long done = reservations.stream().filter(r -> "DONE".equals(r.getStatus())).count();
        long cancelled = reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();

        model.addAttribute("untreatedCount", untreated);
        model.addAttribute("inprogressCount", inprogress);
        model.addAttribute("doneCount", done);
        model.addAttribute("cancelledCount", cancelled);

        return "reservation-list";
    }

    // 2. Scheduler View: Book specific intervals
    @GetMapping("/schedule")
    public String showScheduler(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "description", required = false) String description,
            Model model) {

        LocalDate date = (dateStr == null || dateStr.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);

        model.addAttribute("selectedDate", date);
        model.addAttribute("slots", reservationService.getSlotsForDate(date));
        model.addAttribute("clients", clientService.getAllClients());

        ReservationDto dto = new ReservationDto();
        dto.setDate(date);

        if (clientId != null) {
            dto.setClientId(clientId);
        }
        if (description != null) {
            dto.setDescription(description);
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

        return "redirect:/reservations/schedule?date=" + dto.getDate();
    }

    @PostMapping("/cancel/{id}")
    public String cancelSlot(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "redirect", required = false) String redirectUrl) {

        reservationService.cancelReservation(id, reason);

        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/reservations/schedule?date=" + dateStr;
    }

    @PostMapping("/status/{id}")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam("redirect") String redirectUrl) {

        if ("CANCELLED".equals(status)) {
            reservationService.cancelReservation(id, reason);
        } else {
            reservationService.updateReservationStatus(id, status);
        }

        return "redirect:" + redirectUrl;
    }
}