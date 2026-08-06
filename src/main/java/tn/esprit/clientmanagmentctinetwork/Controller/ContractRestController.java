package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Dto.VisitScheduleDto;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import tn.esprit.clientmanagmentctinetwork.Service.ContractService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractRestController {

    private final ContractService contractService;

    public ContractRestController(ContractService contractService) {
        this.contractService = contractService;
    }

    // 1. GET: Fetch all contracts with optional search, redevance, and status filters [1.2.6]
    @GetMapping
    public ResponseEntity<List<ContractModel>> getAllContracts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String redevanceFilter,
            @RequestParam(value = "activeFilter", required = false) String statusFilter) {
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

    // 8. PUT: Update exact visit dates and file paths (N.D.V constraint-safe) [1.1.4, 1.2.1, 1.2.6]
    @PutMapping("/{id}/schedule-dates")
    public ResponseEntity<ContractModel> updateScheduleDates(
            @PathVariable Long id,
            @RequestBody List<VisitScheduleDto> visits) {
        return ResponseEntity.ok(contractService.updateContractScheduleDates(id, visits));
    }

    // 9. POST: Safe AJAX File Uploader [1.2.6]
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // Creates the uploads/ folder in your project root if missing [1.1.1]
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "uploaded_file";
            }
            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

            // Safe NIO direct byte writing [1.2.1]
            byte[] bytes = file.getBytes();
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + uniqueFilename);
            java.nio.file.Files.write(path, bytes);

            Map<String, String> response = new HashMap<>();
            response.put("filePath", uniqueFilename);
            response.put("fileName", originalFilename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Prints any system exception directly to IntelliJ console [1.1.1]
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    // 10. GET: Safe File Downloader/Viewer stream [1.2.6]
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path path = Paths.get("uploads/").resolve(filename);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 11. GET: Fetch monthly scheduler list
    @GetMapping("/monthly")
    public ResponseEntity<List<ContractModel>> getMonthlySchedules(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        return ResponseEntity.ok(contractService.getMonthlySchedules(month, year));
    }

    // 12. DELETE: Terminate contract
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
}