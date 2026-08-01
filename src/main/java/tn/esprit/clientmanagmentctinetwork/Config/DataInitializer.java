package tn.esprit.clientmanagmentctinetwork.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import tn.esprit.clientmanagmentctinetwork.Repository.SectorRepository;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SectorRepository sectorRepository;

    public DataInitializer(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
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
    }
}