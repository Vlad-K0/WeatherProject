package com.vladko.Controllers;

import com.vladko.DTO.*;
import com.vladko.Entity.Locations;
import com.vladko.Entity.Session;
import com.vladko.Entity.User;
import com.vladko.Exceptions.AuthException;
import com.vladko.Service.LocationService;
import com.vladko.Service.SessionService;
import com.vladko.Service.UserService;
import com.vladko.Service.WeatherApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final SessionService sessionService;
    private final WeatherApiService weatherApiService;
    private final LocationService locationService;

    public ApiController(UserService userService, SessionService sessionService,
            WeatherApiService weatherApiService, LocationService locationService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.weatherApiService = weatherApiService;
        this.locationService = locationService;
    }

    // === Auth Endpoints ===

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            User user = userService.loginUser(authRequest);
            UUID sessionId = sessionService.createSession(user);
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId.toString());
            response.put("username", user.getLogin());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | AuthException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody AuthRequestDTO authRequest) {
        try {
            userService.registerUser(authRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Optional<UUID> sessionIdOpt = extractSessionId(request);
        sessionIdOpt.ifPresent(sessionService::deleteSession);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // === Weather Endpoints ===

    @GetMapping("/weather/current")
    public ResponseEntity<Map<String, Object>> getCurrentWeather(@RequestParam String city) {
        try {
            WeatherApiResponseDTO weather = weatherApiService.getWeather(city);
            Map<String, Object> response = new HashMap<>();
            response.put("city", city);
            response.put("data", weather);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch weather: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/weather/search")
    public ResponseEntity<List<LocationSearchResultDTO>> searchLocations(@RequestParam String q) {
        List<LocationSearchResultDTO> results = weatherApiService.searchLocations(q);
        return ResponseEntity.ok(results);
    }

    // === Location Endpoints (require auth) ===

    @GetMapping("/locations")
    public ResponseEntity<?> getUserLocations(HttpServletRequest request) {
        Optional<User> userOpt = getAuthenticatedUser(request);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Not authenticated"));
        }

        List<Locations> locations = locationService.getUserLocations(userOpt.get());
        List<Map<String, Object>> result = locations.stream().map(loc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", loc.getId());
            map.put("name", loc.getName());
            map.put("latitude", loc.getLatitude());
            map.put("longitude", loc.getLongitude());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/locations")
    public ResponseEntity<?> addLocation(
            @RequestParam String name,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            HttpServletRequest request) {

        Optional<User> userOpt = getAuthenticatedUser(request);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Not authenticated"));
        }

        Locations location = locationService.addLocation(userOpt.get(), name, lat, lon);
        Map<String, Object> response = new HashMap<>();
        response.put("id", location.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Integer id, HttpServletRequest request) {
        Optional<User> userOpt = getAuthenticatedUser(request);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Not authenticated"));
        }

        Optional<Locations> locationOpt = locationService.findById(id);
        if (!locationOpt.isPresent() || !locationOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        }

        locationService.removeLocation(id);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    // === Helper Methods ===

    private Optional<UUID> extractSessionId(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            try {
                return Optional.of(UUID.fromString(authHeader.substring(BEARER_PREFIX.length())));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<User> getAuthenticatedUser(HttpServletRequest request) {
        return extractSessionId(request)
                .flatMap(sessionService::findByToken)
                .map(Session::getUser);
    }
}
