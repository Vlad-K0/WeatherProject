package com.vladko.Controllers;

import com.vladko.DTO.AddLocationDTO;
import com.vladko.Entity.User;
import com.vladko.Service.LocationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
@RequestMapping("/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/add")
    public String addLocation(@RequestParam String name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");

        AddLocationDTO dto = new AddLocationDTO(name, region, country, lat, lon);
        locationService.addLocation(dto, currentUser);

        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Integer id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        locationService.deleteLocation(id, currentUser);
        return "redirect:/";
    }
}
