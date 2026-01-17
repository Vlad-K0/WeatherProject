package com.vladko.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LocationSearchResultDTO {
    private Integer id;
    private String name;
    private String region;
    private String country;
    private BigDecimal lat;
    private BigDecimal lon;
}
