package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Model.SystemSettingModel;
import java.util.List;

public interface SystemSettingService {
    List<SystemSettingModel> getAllSettings();
    void updateSetting(String key, String value);
}