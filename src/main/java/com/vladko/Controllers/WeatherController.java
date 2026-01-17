package com.vladko.Controllers;

import com.vladko.DTO.LocationsWeatherDTO;
import com.vladko.Entity.User;
import com.vladko.Service.LocationService;
import com.vladko.Service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WeatherController {
    private final WeatherApiService weatherApiService;
    private final LocationService locationService;

    public WeatherController(WeatherApiService weatherApiService, LocationService locationService) {
        this.weatherApiService = weatherApiService;
        this.locationService = locationService;
    }

    @GetMapping("/")
    public String showMainPage(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser != null) {
            LocationsWeatherDTO userLocations = locationService.getUserLocations(currentUser.getLogin());
            model.addAttribute("locations", userLocations.getLocations());
            model.addAttribute("username", currentUser.getLogin());
        }
        return "index";
    }

    @GetMapping("/search")
    public String searchCities(@RequestParam(required = false) String query,
            HttpServletRequest request,
            Model model) throws IOException {
        User currentUser = (User) request.getAttribute("currentUser");
        model.addAttribute("username", currentUser.getLogin());

        if (query != null && !query.trim().isEmpty()) {
            LocationsWeatherDTO searchResults = weatherApiService.searchLocations(query);
            model.addAttribute("searchResults", searchResults.getLocations());
            model.addAttribute("query", query);
        }

        return "search";
    }

    public static class LocationWeatherData {
        private final Locations location;
        private final WeatherApiResponseDTO weather;

        public LocationWeatherData(Locations location, WeatherApiResponseDTO weather) {
            this.location = location;
            this.weather = weather;
        }

        public Locations getLocation() {
            return location;
        }

        public WeatherApiResponseDTO getWeather() {
            return weather;
        }
    }
}
