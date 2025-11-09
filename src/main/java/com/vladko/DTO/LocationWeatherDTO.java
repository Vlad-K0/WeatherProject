package com.vladko.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationWeatherDTO {
    String name;
    String region;
    String country;
    double lat;
    double lon;
}
