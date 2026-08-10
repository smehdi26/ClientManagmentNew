package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.*;
import tn.esprit.clientmanagmentctinetwork.Repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200") // Permit Angular access
public class AnalyticsRestController {

    private final ClientRepository clientRepository;
    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;

    public AnalyticsRestController(ClientRepository clientRepository,
                                   ContractRepository contractRepository,
                                   ReservationRepository reservationRepository) {
        this.clientRepository = clientRepository;
        this.contractRepository = contractRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Sector Distribution (Pie Chart)
        stats.put("clientsBySector", clientRepository.findAll().stream()
                .filter(c -> c.getSector() != null)
                .collect(Collectors.groupingBy(c -> c.getSector().getName(), Collectors.counting())));

        // 2. Reservation Status Distribution (Donut Chart)
        stats.put("reservationStatus", reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(ReservationModel::getStatus, Collectors.counting())));

        // 3. Contract Types (Bar Chart)
        stats.put("contractsByType", contractRepository.findAll().stream()
                .collect(Collectors.groupingBy(ContractModel::getRedevance, Collectors.counting())));

        // 4. Tunisian Governorate Distribution (Treemap)
        stats.put("cityDistribution", clientRepository.findAll().stream()
                .filter(c -> c.getCity() != null)
                .collect(Collectors.groupingBy(ClientModel::getCity, Collectors.counting())));

        // 5. IT Technician Workload (Horizontal Bar)
        stats.put("techWorkload", reservationRepository.findAll().stream()
                .filter(r -> r.getTechnician() != null)
                .collect(Collectors.groupingBy(r -> r.getTechnician().getFirstName() + " " + r.getTechnician().getLastName(), Collectors.counting())));

        // 6. Maintenance Execution Rate (Radial Chart)
        // Logic: Performed = has date | Documented = has date + has file
        List<ContractModel> contracts = contractRepository.findAll();
        long totalRequired = contracts.stream().mapToLong(ContractModel::getNumberOfVisits).sum();
        long performed = 0;
        long documented = 0;

        for (ContractModel c : contracts) {
            // Check slot 1
            if (c.getVisitDate1() != null) {
                performed++;
                if (c.getVisitFile1Raw() != null) documented++;
            }
            // Check slot 2
            if (c.getVisitDate2() != null) {
                performed++;
                if (c.getVisitFile2Raw() != null) documented++;
            }
            // Check slot 3
            if (c.getVisitDate3() != null) {
                performed++;
                if (c.getVisitFile3Raw() != null) documented++;
            }
            // Check slot 4
            if (c.getVisitDate4() != null) {
                performed++;
                if (c.getVisitFile4Raw() != null) documented++;
            }
            // Check slot 5
            if (c.getVisitDate5() != null) {
                performed++;
                if (c.getVisitFile5Raw() != null) documented++;
            }
            // Check slot 6
            if (c.getVisitDate6() != null) {
                performed++;
                if (c.getVisitFile6Raw() != null) documented++;
            }
        }

        Map<String, Object> exec = new HashMap<>();
        exec.put("totalRequired", totalRequired);
        exec.put("performed", performed);
        exec.put("documented", documented);
        exec.put("performedPct", totalRequired > 0 ? (performed * 100 / totalRequired) : 0);
        exec.put("documentedPct", performed > 0 ? (documented * 100 / performed) : 0);
        stats.put("executionRate", exec);

        // 7. Monthly Reservation Trends (Grouped Column Chart)
        // Groups by the 12 months of the CURRENT YEAR
        int currentYear = LocalDate.now().getYear();
        List<ReservationModel> currentYearRes = reservationRepository.findAll().stream()
                .filter(r -> r.getReservationTime() != null && r.getReservationTime().getYear() == currentYear)
                .collect(Collectors.toList());

        String[] statuses = {"UNTREATED", "IN_PROGRESS", "DONE", "CANCELLED"};
        Map<String, List<Long>> monthlySeries = new HashMap<>();

        for (String status : statuses) {
            Long[] monthData = new Long[12];
            Arrays.fill(monthData, 0L);

            for (ReservationModel r : currentYearRes) {
                if (r.getStatus().equals(status)) {
                    int monthIdx = r.getReservationTime().getMonthValue() - 1; // 0-11
                    monthData[monthIdx]++;
                }
            }
            monthlySeries.put(status, Arrays.asList(monthData));
        }

        stats.put("reservationMonthly", monthlySeries);
        stats.put("monthLabels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));

        return ResponseEntity.ok(stats);
    }
}