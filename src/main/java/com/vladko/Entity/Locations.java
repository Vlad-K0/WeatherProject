package com.vladko.Entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "locations")
public class Locations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false,  unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(length = 50, nullable = false)
    private BigDecimal latitude;

    private BigDecimal longitude;

}
