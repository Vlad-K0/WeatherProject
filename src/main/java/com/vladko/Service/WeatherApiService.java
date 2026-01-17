package com.vladko.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladko.DTO.LocationWeatherDTO;
import com.vladko.DTO.LocationsWeatherDTO;
import com.vladko.Utils.PropertyParsers.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class WeatherApiService {
    private static final String API_KEY = "b1fe6dd623f94a799a4123107250911";
    private static final String BASE_URL = "http://api.weatherapi.com/v1";

    @Value("${weather-api.base-url}")
    private String baseURL;

    @Value("${weather-api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public WeatherApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LocationsWeatherDTO searchLocations(String cityName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/search.json")
                .queryParam("key", apiKey)
                .queryParam("q", cityName)
                .toUriString();

        try {
            String locationsJson = restTemplate.getForObject(url, String.class);

            if (locationsJson == null || locationsJson.isEmpty()) {
                return new LocationsWeatherDTO(java.util.Collections.emptyList());
            }

            LocationWeatherDTO[] locationsArray = mapper.readValue(locationsJson, LocationWeatherDTO[].class);
            LocationsWeatherDTO result = new LocationsWeatherDTO();
            result.setLocations(java.util.Arrays.asList(locationsArray));

            return result;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            return new LocationsWeatherDTO(java.util.Collections.emptyList());
        } catch (org.springframework.web.client.RestClientException e) {
            return new LocationsWeatherDTO(java.util.Collections.emptyList());
        }
    }

    public LocationWeatherDTO getWeatherByCoordinates(double lat, double lon) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/current.json")
                .queryParam("key", apiKey)
                .queryParam("q", lat + "," + lon)
                .toUriString();
        String locationJson = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = mapper.readTree(locationJson);
            LocationWeatherDTO dto = new LocationWeatherDTO();
            dto.setName(root.path("location").path("name").asText());
            dto.setLat(new java.math.BigDecimal(root.path("location").path("lat").asDouble()));
            dto.setLon(new java.math.BigDecimal(root.path("location").path("lon").asDouble()));
            dto.setTemperature(root.path("current").path("temp_c").asDouble());
            return dto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LocationSearchResultDTO> searchLocations(String query) {
        try {
            String url = BASE_URL + "/search.json?key=" + API_KEY + "&q=" + query;
            String jsonString = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(jsonString, new TypeReference<List<LocationSearchResultDTO>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
