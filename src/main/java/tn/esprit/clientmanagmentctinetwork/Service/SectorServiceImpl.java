package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import tn.esprit.clientmanagmentctinetwork.Repository.SectorRepository;
import java.util.List;

@Service
@Transactional
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;

    public SectorServiceImpl(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    @Override
    public List<SectorModel> getAllSectors() {
        return sectorRepository.findAll();
    }

    @Override
    public List<SectorModel> getActiveSectors() {
        return sectorRepository.findByActiveTrue();
    }

    @Override
    public SectorModel saveSector(SectorModel sector) {
        return sectorRepository.save(sector);
    }

    @Override
    public SectorModel updateSectorStatus(Long id, boolean active) {
        SectorModel sector = sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sector not found"));
        sector.setActive(active);
        return sectorRepository.save(sector);
    }
}