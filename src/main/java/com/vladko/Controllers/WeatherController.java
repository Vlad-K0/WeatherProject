package com.vladko.Controllers;


import com.vladko.DTO.WeatherApiResponseDTO;
import com.vladko.Service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class WeatherController {
    private final WeatherApiService weatherApiService = new WeatherApiService();
    @GetMapping("/getWeatherInCity")
    public String Weather (@RequestParam String city, Model model) throws IOException {
        WeatherApiResponseDTO weatherApiResponseDTO = weatherApiService.GetWeather(city);
        model.addAttribute("weather", weatherApiResponseDTO);
        return "weather/weather";
    }
    @GetMapping("/")
    public String showMainPage(){
        return "weather/search";
    }
}
