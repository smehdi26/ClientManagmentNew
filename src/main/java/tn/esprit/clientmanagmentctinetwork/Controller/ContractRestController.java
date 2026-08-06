package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import tn.esprit.clientmanagmentctinetwork.Service.ContractService;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractRestController {

    private final ContractService contractService;

    public ContractRestController(ContractService contractService) {
        this.contractService = contractService;
    }

    // 1. GET: Fetch all contracts with optional search, term, and status filters [1.2.6]
    @GetMapping
    public ResponseEntity<List<ContractModel>> getAllContracts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String redevanceFilter,
            @RequestParam(value = "activeFilter", required = false) String statusFilter) { // Added parameter
        return ResponseEntity.ok(contractService.searchAndFilterContracts(keyword, redevanceFilter, statusFilter));
    }

    // 2. POST: Register new contract
    @PostMapping
    public ResponseEntity<ContractModel> createContract(@RequestBody ContractDto dto) {
        ContractModel saved = contractService.createContract(dto);
        return ResponseEntity.ok(saved);
    }

    // 3. GET: List contracts by Client ID
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContractModel>> getContractsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contractService.getContractsByClientId(clientId));
    }

    // 4. GET: Fetch contract by ID (Details Workspace)
    @GetMapping("/{id}")
    public ResponseEntity<ContractModel> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    // 5. PUT: Update contract details
    @PutMapping("/{id}")
    public ResponseEntity<ContractModel> updateContract(@PathVariable Long id, @RequestBody ContractDto dto) {
        return ResponseEntity.ok(contractService.updateContract(id, dto));
    }

    // 6. PUT: Toggle contract active/suspended status
    @PutMapping("/{id}/status")
    public ResponseEntity<ContractModel> updateStatus(@PathVariable Long id, @RequestParam("status") String status) {
        return ResponseEntity.ok(contractService.updateStatus(id, status));
    }

    // 7. POST: Renew contract (Adds exactly 1 year and resets schedule) [1.1.4]
    @PostMapping("/{id}/renew")
    public ResponseEntity<ContractModel> renewContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.renewContract(id));
    }

    // 8. PUT: Update exact visit dates (N.D.V constraint-safe) [1.1.4, 1.2.1]
    @PutMapping("/{id}/schedule-dates")
    public ResponseEntity<ContractModel> updateScheduleDates(
            @PathVariable Long id,
            @RequestBody List<java.time.LocalDate> dates) {
        return ResponseEntity.ok(contractService.updateContractScheduleDates(id, dates));
    }

    // 9. GET: Fetch monthly scheduler list
    @GetMapping("/monthly")
    public ResponseEntity<List<ContractModel>> getMonthlySchedules(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        return ResponseEntity.ok(contractService.getMonthlySchedules(month, year));
    }

    // 10. DELETE: Terminate contract
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
}