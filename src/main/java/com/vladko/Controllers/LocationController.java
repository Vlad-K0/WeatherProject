package com.vladko.Controllers;

import com.vladko.DTO.LocationSearchResultDTO;
import com.vladko.Entity.User;
import com.vladko.Service.LocationService;
import com.vladko.Service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationController {
    private final LocationService locationService;
    private final WeatherApiService weatherApiService;

    public LocationController(LocationService locationService, WeatherApiService weatherApiService) {
        this.locationService = locationService;
        this.weatherApiService = weatherApiService;
    }

    @GetMapping("/search")
    public String searchLocations(@RequestParam String query, Model model) {
        List<LocationSearchResultDTO> results = weatherApiService.searchLocations(query);
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "weather/search-results";
    }

    @PostMapping
    public String addLocation(
            @RequestParam String name,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            HttpServletRequest request) {

        User currentUser = (User) request.getAttribute(AuthenticationInterceptor.CURRENT_USER_ATTR);
        if (currentUser != null) {
            locationService.addLocation(currentUser, name, lat, lon);
        }
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Integer id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute(AuthenticationInterceptor.CURRENT_USER_ATTR);
        if (currentUser != null) {
            locationService.findById(id)
                    .filter(loc -> loc.getUser().getId().equals(currentUser.getId()))
                    .ifPresent(loc -> locationService.removeLocation(id));
        }
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteLocationPost(@RequestParam Integer locationId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute(AuthenticationInterceptor.CURRENT_USER_ATTR);
        if (currentUser != null) {
            locationService.findById(locationId)
                    .filter(loc -> loc.getUser().getId().equals(currentUser.getId()))
                    .ifPresent(loc -> locationService.removeLocation(locationId));
        }
        return "redirect:/";
    }
}
