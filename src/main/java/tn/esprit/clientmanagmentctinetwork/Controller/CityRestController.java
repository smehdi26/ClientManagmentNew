package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.CityModel;
import tn.esprit.clientmanagmentctinetwork.Service.CityService;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityRestController {

    private final CityService cityService;

    public CityRestController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public ResponseEntity<List<CityModel>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CityModel>> getActiveCities() {
        return ResponseEntity.ok(cityService.getActiveCities());
    }

    @PostMapping
    public ResponseEntity<CityModel> createCity(@RequestBody CityModel city) {
        return ResponseEntity.ok(cityService.saveCity(city));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CityModel> toggleStatus(@PathVariable Long id, @RequestParam("active") boolean active) {
        return ResponseEntity.ok(cityService.updateCityStatus(id, active));
    }
}