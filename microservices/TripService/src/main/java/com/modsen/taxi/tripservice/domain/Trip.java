package com.modsen.taxi.tripservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long driverId;
    private Long passengerId;

    private String originAddress;
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    private TripStatus status;

    private LocalDateTime orderDateTime;

    private BigDecimal price;
}
