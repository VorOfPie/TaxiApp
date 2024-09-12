package com.modsen.taxi.driversrvice.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private String color;

    private String licensePlate;

    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
}
