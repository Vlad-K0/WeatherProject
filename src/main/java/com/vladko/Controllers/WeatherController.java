package com.vladko.Controllers;

import com.vladko.DTO.WeatherApiResponseDTO;
import com.vladko.Entity.Locations;
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

    @GetMapping("/getWeatherInCity")
    public String getWeather(@RequestParam String city, Model model) throws IOException {
        WeatherApiResponseDTO weatherApiResponseDTO = weatherApiService.getWeather(city);
        model.addAttribute("weather", weatherApiResponseDTO);
        return "weather/weather";
    }

    @GetMapping("/")
    public String showMainPage(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute(AuthenticationInterceptor.CURRENT_USER_ATTR);
        if (currentUser != null) {
            List<Locations> userLocations = locationService.getUserLocations(currentUser);
            List<LocationWeatherData> weatherDataList = new ArrayList<>();

            for (Locations location : userLocations) {
                try {
                    WeatherApiResponseDTO weather = weatherApiService.getWeather(location.getName());
                    weatherDataList.add(new LocationWeatherData(location, weather));
                } catch (IOException e) {
                    weatherDataList.add(new LocationWeatherData(location, null));
                }
            }
            model.addAttribute("locations", weatherDataList);
            model.addAttribute("username", currentUser.getLogin());
        }
        return "weather/search";
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
