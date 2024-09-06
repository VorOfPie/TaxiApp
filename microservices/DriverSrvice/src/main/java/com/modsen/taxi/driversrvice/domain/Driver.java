package com.modsen.taxi.driversrvice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String phone;

    private String gender;

    private Boolean isDeleted;

    @OneToMany(mappedBy = "driver", fetch = FetchType.EAGER)
    private List<Car> cars;
}
