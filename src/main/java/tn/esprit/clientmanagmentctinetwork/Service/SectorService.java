package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import java.util.List;

public interface SectorService {
    List<SectorModel> getAllSectors();
    List<SectorModel> getActiveSectors();
    SectorModel saveSector(SectorModel sector);
    SectorModel updateSectorStatus(Long id, boolean active);
}