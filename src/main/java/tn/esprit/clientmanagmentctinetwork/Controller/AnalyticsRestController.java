package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.*;
import tn.esprit.clientmanagmentctinetwork.Repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AnalyticsRestController {

    private final ClientRepository clientRepository;
    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public AnalyticsRestController(ClientRepository clientRepository,
                                   ContractRepository contractRepository,
                                   ReservationRepository reservationRepository,
                                   UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.contractRepository = contractRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // --- 0. SHARED CONSTANTS ---
        int currentYear = LocalDate.now().getYear(); // Defined ONCE at the top
        List<String> monthLabels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        stats.put("monthLabels", monthLabels);

        // --- 1. GENERAL KPI: TOTAL CLIENTS ---
        stats.put("totalClients", clientRepository.count());

        // --- 2. CLIENT ACQUISITION TREND (AREA CHART DATA) ---
        Long[] growthData = new Long[12];
        Arrays.fill(growthData, 0L);
        List<ClientModel> allClients = clientRepository.findAll();
        for (ClientModel c : allClients) {
            // Only count clients created in the current year
            if (c.getCreatedAt() != null && c.getCreatedAt().getYear() == currentYear) {
                int monthIdx = c.getCreatedAt().getMonthValue() - 1; // 0-11
                growthData[monthIdx]++;
            }
        }
        // This is initialized to 0 to support the chart; if you add a 'createdAt' field to ClientModel,
        // you can populate this dynamically here later.
        stats.put("clientMonthlyGrowth", Arrays.asList(growthData));

        // --- 3. SECTOR DISTRIBUTION (PIE CHART) ---
        stats.put("clientsBySector", clientRepository.findAll().stream()
                .filter(c -> c.getSector() != null)
                .collect(Collectors.groupingBy(c -> c.getSector().getName(), Collectors.counting())));

        // --- 4. RESERVATION STATUS DISTRIBUTION (DONUT CHART) ---
        stats.put("reservationStatus", reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(ReservationModel::getStatus, Collectors.counting())));

        // --- 5. CONTRACT TYPES (BAR CHART) ---
        stats.put("contractsByType", contractRepository.findAll().stream()
                .collect(Collectors.groupingBy(ContractModel::getRedevance, Collectors.counting())));

        // --- 6. TUNISIAN GOVERNORATE DISTRIBUTION (TREEMAP) ---
        stats.put("cityDistribution", clientRepository.findAll().stream()
                .filter(c -> c.getCity() != null)
                .collect(Collectors.groupingBy(ClientModel::getCity, Collectors.counting())));

        // --- 7. IT TECHNICIAN WORKLOAD (HORIZONTAL BAR) ---
        stats.put("techWorkload", reservationRepository.findAll().stream()
                .filter(r -> r.getTechnician() != null)
                .collect(Collectors.groupingBy(r -> r.getTechnician().getFirstName() + " " + r.getTechnician().getLastName(), Collectors.counting())));

        // --- 8. MAINTENANCE EXECUTION & DOCUMENTATION RATE (RADIAL CHART) ---
        // Detailed check for Performed (Date exists) vs Documented (File exists)
        List<ContractModel> contracts = contractRepository.findAll();
        long totalRequired = contracts.stream().mapToLong(ContractModel::getNumberOfVisits).sum();
        long performed = 0;
        long documented = 0;

        for (ContractModel c : contracts) {
            // Slot 1
            if (c.getVisitDate1() != null) {
                performed++;
                if (c.getVisitFile1Raw() != null) documented++;
            }
            // Slot 2
            if (c.getVisitDate2() != null) {
                performed++;
                if (c.getVisitFile2Raw() != null) documented++;
            }
            // Slot 3
            if (c.getVisitDate3() != null) {
                performed++;
                if (c.getVisitFile3Raw() != null) documented++;
            }
            // Slot 4
            if (c.getVisitDate4() != null) {
                performed++;
                if (c.getVisitFile4Raw() != null) documented++;
            }
            // Slot 5
            if (c.getVisitDate5() != null) {
                performed++;
                if (c.getVisitFile5Raw() != null) documented++;
            }
            // Slot 6
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

        // --- 9. MONTHLY RESERVATION TRENDS (GROUPED COLUMN CHART) ---
        // Uses the 'currentYear' variable from the top scope
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

        return ResponseEntity.ok(stats);
    }
}