package com.vladko.Service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladko.DTO.WeatherApiResponseDTO;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class WeatherApiService {

    public WeatherApiResponseDTO GetWeather(String city) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://api.weatherapi.com/v1/current.json?key=b1fe6dd623f94a799a4123107250911&q=" + city;
        String jsonString = restTemplate.getForObject(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        return objectMapper.readValue(jsonString, WeatherApiResponseDTO.class);


    }

}
