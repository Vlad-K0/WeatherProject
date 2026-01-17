package com.vladko.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddLocationDTO {
    private String name;
    private String region;
    private String country;
    private BigDecimal lat;
    private BigDecimal lon;
}
