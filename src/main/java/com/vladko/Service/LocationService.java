package com.vladko.Service;

import com.vladko.DTO.LocationWeatherDTO;
import com.vladko.DTO.LocationsWeatherDTO;
import com.vladko.Entity.Location;
import com.vladko.Repositories.LocationRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationsWeatherDTO getUserLocations(String username) {
        ArrayList<Location> locations = (ArrayList<Location>) locationRepository.getLocationsByUserName(username);
        List<LocationWeatherDTO> locationDTOs = locations.stream()
                .map(location -> new LocationWeatherDTO(
                        location.getName(),
                        location.getLatitude(),
                        location.getLongitude()
                )).collect(Collectors.toList());
        return new LocationsWeatherDTO(locationDTOs);
    }

}
