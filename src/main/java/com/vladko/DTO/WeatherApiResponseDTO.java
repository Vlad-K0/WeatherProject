package com.vladko.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherApiResponseDTO {
    private LocationWeatherDTO location;
    private CurrentWeatherDTO current;
}
