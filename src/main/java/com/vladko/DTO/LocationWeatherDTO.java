package com.vladko.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationWeatherDTO {
    String name;
    BigDecimal lat;
    BigDecimal lon;
}
