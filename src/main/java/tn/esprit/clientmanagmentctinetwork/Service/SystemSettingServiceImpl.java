package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.SystemSettingModel;
import tn.esprit.clientmanagmentctinetwork.Repository.SystemSettingRepository;
import java.util.List;

@Service
@Transactional
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingServiceImpl(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public List<SystemSettingModel> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    @Override
    public void updateSetting(String key, String value) {
        SystemSettingModel setting = systemSettingRepository.findByKey(key)
                .orElseGet(() -> new SystemSettingModel(key, value));
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }
}