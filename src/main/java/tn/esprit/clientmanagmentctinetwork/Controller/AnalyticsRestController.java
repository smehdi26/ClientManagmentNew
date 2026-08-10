package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.*;
import tn.esprit.clientmanagmentctinetwork.Repository.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
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

        // 1. Sector Distribution
        stats.put("clientsBySector", clientRepository.findAll().stream()
                .filter(c -> c.getSector() != null)
                .collect(Collectors.groupingBy(c -> c.getSector().getName(), Collectors.counting())));

        // 2. Status Distribution
        stats.put("reservationStatus", reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(ReservationModel::getStatus, Collectors.counting())));

        // 3. Contract Types
        stats.put("contractsByType", contractRepository.findAll().stream()
                .collect(Collectors.groupingBy(ContractModel::getRedevance, Collectors.counting())));

        // 4. City Distribution
        stats.put("cityDistribution", clientRepository.findAll().stream()
                .filter(c -> c.getCity() != null)
                .collect(Collectors.groupingBy(ClientModel::getCity, Collectors.counting())));

        // 5. Tech Workload
        stats.put("techWorkload", reservationRepository.findAll().stream()
                .filter(r -> r.getTechnician() != null)
                .collect(Collectors.groupingBy(r -> r.getTechnician().getFirstName() + " " + r.getTechnician().getLastName(), Collectors.counting())));

        // 6. Execution Rate
        List<ContractModel> contracts = contractRepository.findAll();
        long total = contracts.stream().mapToLong(ContractModel::getNumberOfVisits).sum();
        long completed = contracts.stream().mapToLong(c -> {
            long count = 0;
            if (c.getVisitFile1Raw() != null) count++;
            if (c.getVisitFile2Raw() != null) count++;
            if (c.getVisitFile3Raw() != null) count++;
            if (c.getVisitFile4Raw() != null) count++;
            if (c.getVisitFile5Raw() != null) count++;
            if (c.getVisitFile6Raw() != null) count++;
            return count;
        }).sum();

        Map<String, Object> exec = new HashMap<>();
        exec.put("total", total);
        exec.put("completed", completed);
        exec.put("percentage", total > 0 ? (completed * 100 / total) : 0);
        stats.put("executionRate", exec);

        return ResponseEntity.ok(stats);
    }
}