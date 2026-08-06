package tn.esprit.clientmanagmentctinetwork.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.clientmanagmentctinetwork.Model.SystemSettingModel;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSettingModel, Long> {
    Optional<SystemSettingModel> findByKey(String key);
}