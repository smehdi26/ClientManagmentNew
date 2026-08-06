package tn.esprit.clientmanagmentctinetwork.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.clientmanagmentctinetwork.Model.CityModel;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import tn.esprit.clientmanagmentctinetwork.Model.SystemSettingModel;
import tn.esprit.clientmanagmentctinetwork.Repository.CityRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.SectorRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.SystemSettingRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SectorRepository sectorRepository;
    private final CityRepository cityRepository;
    private final SystemSettingRepository systemSettingRepository;

    public DataInitializer(SectorRepository sectorRepository,
                           CityRepository cityRepository,
                           SystemSettingRepository systemSettingRepository) {
        this.sectorRepository = sectorRepository;
        this.cityRepository = cityRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed default Sectors
        if (sectorRepository.count() == 0) {
            List<String> defaultSectors = Arrays.asList(
                    "Information Technology / Software Development",
                    "Commerce",
                    "Manufacturing",
                    "Construction and Public Works",
                    "Transportation and Logistics",
                    "Healthcare",
                    "Education and Training",
                    "Banking and Insurance",
                    "Tourism, Hospitality and Food Service",
                    "Agriculture and Food Processing"
            );
            for (String name : defaultSectors) {
                SectorModel sector = new SectorModel();
                sector.setName(name);
                sector.setActive(true);
                sectorRepository.save(sector);
            }
        }

        // 2. Seed default Cities (24 Tunisian Governorates) [1.1.1, 1.1.4]
        if (cityRepository.count() == 0) {
            List<String> defaultCities = Arrays.asList(
                    "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                    "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia",
                    "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
                    "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
            );
            for (String name : defaultCities) {
                CityModel city = new CityModel();
                city.setName(name);
                city.setActive(true);
                cityRepository.save(city);
            }
        }

        // 3. Seed default System Settings
        if (systemSettingRepository.count() == 0) {
            systemSettingRepository.save(new SystemSettingModel("EMAIL_NOTIFICATIONS", "true"));
            systemSettingRepository.save(new SystemSettingModel("BROWSER_NOTIFICATIONS", "true"));
            systemSettingRepository.save(new SystemSettingModel("RESERVATION_REMINDERS", "true"));
            systemSettingRepository.save(new SystemSettingModel("CONTRACT_REMINDERS", "true"));
        }
    }
}