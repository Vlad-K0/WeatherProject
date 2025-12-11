package com.vladko.Controllers;

import com.vladko.DTO.*;
import com.vladko.Entity.Locations;
import com.vladko.Entity.RefreshToken;
import com.vladko.Entity.User;
import com.vladko.Exceptions.AuthException;
import com.vladko.Repositories.RefreshTokenRepository;
import com.vladko.Repositories.UserRepository;
import com.vladko.Service.JwtService;
import com.vladko.Service.LocationService;
import com.vladko.Service.UserService;
import com.vladko.Service.WeatherApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WeatherApiService weatherApiService;
    private final LocationService locationService;

    public ApiController(UserService userService, UserRepository userRepository,
            JwtService jwtService, RefreshTokenRepository refreshTokenRepository,
            WeatherApiService weatherApiService, LocationService locationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.weatherApiService = weatherApiService;
        this.locationService = locationService;
    }

    // === Auth Endpoints ===

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            User user = userService.loginUser(authRequest);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Store refresh token in DB
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshToken)
                    .user(user)
                    .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(refreshTokenEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("username", user.getLogin());
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 900); // 15 minutes in seconds
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

    @PostMapping("/auth/refresh")
    public ResponseEntity<Map<String, Object>> refreshTokens(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid refresh token"));
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Token is not a refresh token"));
        }

        // Check if token exists in DB and not revoked
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(refreshToken);
        if (!storedToken.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Refresh token not found or revoked"));
        }

        // Get user and generate new tokens
        Integer userId = jwtService.getUserIdFromToken(refreshToken);
        Optional<User> userOpt = userRepository.findById(userId);

        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "User not found"));
        }

        User user = userOpt.get();

        // Revoke old refresh token
        RefreshToken oldToken = storedToken.get();
        oldToken.setRevoked(true);
        refreshTokenRepository.update(oldToken);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Store new refresh token
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 900);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Optional<User> userOpt = getAuthenticatedUser(request);
        userOpt.ifPresent(refreshTokenRepository::revokeAllUserTokens);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
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

    private Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.of(authHeader.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    private Optional<User> getAuthenticatedUser(HttpServletRequest request) {
        return extractToken(request)
                .filter(jwtService::validateToken)
                .filter(jwtService::isAccessToken)
                .map(jwtService::getUserIdFromToken)
                .flatMap(userRepository::findById);
    }
}
