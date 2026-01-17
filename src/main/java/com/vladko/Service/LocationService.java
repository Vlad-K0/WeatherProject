package com.vladko.Service;

import com.vladko.Entity.Locations;
import com.vladko.Entity.User;
import com.vladko.Repositories.LocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Locations addLocation(User user, String name, BigDecimal latitude, BigDecimal longitude) {
        Locations location = new Locations();
        location.setUser(user);
        location.setName(name);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return locationRepository.save(location);
    }

    public void removeLocation(Integer id) {
        locationRepository.delete(id);
    }

    public List<Locations> getUserLocations(User user) {
        return locationRepository.findByUser(user);
    }

    public Optional<Locations> findById(Integer id) {
        return locationRepository.findById(id);
    }
}
