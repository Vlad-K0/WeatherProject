package com.vladko.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherDTO {
    float temp_c;
    float feelslike_c;
}

