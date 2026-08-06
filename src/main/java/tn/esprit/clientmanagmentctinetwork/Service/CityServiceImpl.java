package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.CityModel;
import tn.esprit.clientmanagmentctinetwork.Repository.CityRepository;
import java.util.List;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public List<CityModel> getAllCities() {
        return cityRepository.findAll();
    }

    @Override
    public List<CityModel> getActiveCities() {
        return cityRepository.findByActiveTrue();
    }

    @Override
    public CityModel saveCity(CityModel city) {
        return cityRepository.save(city);
    }

    @Override
    public CityModel updateCityStatus(Long id, boolean active) {
        CityModel city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        city.setActive(active);
        return cityRepository.save(city);
    }
}