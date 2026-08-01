package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import tn.esprit.clientmanagmentctinetwork.Service.SectorService;
import java.util.List;

@RestController
@RequestMapping("/api/sectors")
public class SectorRestController {

    private final SectorService sectorService;

    public SectorRestController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @GetMapping
    public ResponseEntity<List<SectorModel>> getAllSectors() {
        return ResponseEntity.ok(sectorService.getAllSectors());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SectorModel>> getActiveSectors() {
        return ResponseEntity.ok(sectorService.getActiveSectors());
    }

    @PostMapping
    public ResponseEntity<SectorModel> createSector(@RequestBody SectorModel sector) {
        return ResponseEntity.ok(sectorService.saveSector(sector));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SectorModel> toggleStatus(@PathVariable Long id, @RequestParam("active") boolean active) {
        return ResponseEntity.ok(sectorService.updateSectorStatus(id, active));
    }
}