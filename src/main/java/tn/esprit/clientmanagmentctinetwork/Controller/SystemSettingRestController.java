package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Model.SystemSettingModel;
import tn.esprit.clientmanagmentctinetwork.Service.SystemSettingService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingRestController {

    private final SystemSettingService systemSettingService;

    public SystemSettingRestController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @GetMapping
    public ResponseEntity<List<SystemSettingModel>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PutMapping
    public ResponseEntity<Void> updateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok().build();
    }
}