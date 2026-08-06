package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.CityModel;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<CityModel, Long> {
    List<CityModel> findByActiveTrue();
}