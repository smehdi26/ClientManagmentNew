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

    @PostMapping
    public ResponseEntity<ContractModel> createContract(@RequestBody ContractDto dto) {
        ContractModel saved = contractService.createContract(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContractModel>> getContractsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contractService.getContractsByClientId(clientId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContractModel>> getAllContracts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "redevanceFilter", required = false) String redevanceFilter) {
        return ResponseEntity.ok(contractService.searchAndFilterContracts(keyword, redevanceFilter));
    }

    @PutMapping("/{id}/schedule")
    public ResponseEntity<ContractModel> updateContractSchedule(
            @PathVariable Long id,
            @RequestParam("months") String months) {
        ContractModel updated = contractService.updateContractSchedule(id, months);
        return ResponseEntity.ok(updated);
    }
}