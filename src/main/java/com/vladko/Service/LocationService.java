package com.vladko.Service;

import com.vladko.DTO.AddLocationDTO;
import com.vladko.DTO.LocationWeatherDTO;
import com.vladko.DTO.LocationsWeatherDTO;
import com.vladko.Entity.Location;
import com.vladko.Entity.User;
import com.vladko.Repositories.LocationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationService {
    private final LocationRepository locationRepository;
    private final WeatherApiService weatherApiService;

    public LocationService(LocationRepository locationRepository, WeatherApiService weatherApiService) {
        this.locationRepository = locationRepository;
        this.weatherApiService = weatherApiService;
    }

    public void addLocation(AddLocationDTO dto, User user) {
        Location location = new Location();
        location.setName(dto.getName());
        location.setRegion(dto.getRegion());
        location.setCountry(dto.getCountry());
        location.setLatitude(dto.getLat());
        location.setLongitude(dto.getLon());
        location.setUser(user);

        try {
            locationRepository.save(location);
        } catch (DataIntegrityViolationException | javax.persistence.PersistenceException e) {
            // Локация уже существует - игнорируем
        }
    }

    public void deleteLocation(Integer locationId, User user) {
        Location location = locationRepository.findById(locationId).orElse(null);
        if (location != null && location.getUser().getId().equals(user.getId())) {
            locationRepository.delete(locationId);
        }
    }

    public LocationsWeatherDTO getUserLocations(String username) {
        List<Location> locations = locationRepository.getLocationsByUserName(username);
        List<LocationWeatherDTO> locationDTOs = locations.stream()
                .map(location -> {
                    LocationWeatherDTO dto = new LocationWeatherDTO();
                    dto.setId(location.getId());
                    dto.setName(location.getName());
                    dto.setRegion(location.getRegion());
                    dto.setCountry(location.getCountry());
                    dto.setLat(location.getLatitude());
                    dto.setLon(location.getLongitude());
                    try {
                        LocationWeatherDTO weather = weatherApiService.getWeatherByCoordinates(
                                location.getLatitude().doubleValue(),
                                location.getLongitude().doubleValue());
                        dto.setTemperature(weather.getTemperature());
                    } catch (Exception e) {
                        dto.setTemperature(null);
                    }

                    return dto;
                }).collect(Collectors.toList());
        return new LocationsWeatherDTO(locationDTOs);
    }

}
