package com.vladko.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladko.DTO.LocationSearchResultDTO;
import com.vladko.DTO.WeatherApiResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class WeatherApiService {
    private static final String API_KEY = "b1fe6dd623f94a799a4123107250911";
    private static final String BASE_URL = "http://api.weatherapi.com/v1";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public WeatherApiService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public WeatherApiResponseDTO getWeather(String city) throws IOException {
        String url = BASE_URL + "/current.json?key=" + API_KEY + "&q=" + city;
        String jsonString = restTemplate.getForObject(url, String.class);
        return objectMapper.readValue(jsonString, WeatherApiResponseDTO.class);
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
