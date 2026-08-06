package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Model.CityModel;
import java.util.List;

public interface CityService {
    List<CityModel> getAllCities();
    List<CityModel> getActiveCities();
    CityModel saveCity(CityModel city);
    CityModel updateCityStatus(Long id, boolean active);
}