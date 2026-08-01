package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import java.util.List;

@Repository
public interface SectorRepository extends JpaRepository<SectorModel, Long> {
    List<SectorModel> findByActiveTrue();
}