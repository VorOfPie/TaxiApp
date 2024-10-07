package com.modsen.taxi.passengerservice;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = PassengerServiceApplication.class)
public class CucumberSpringConfiguration {
}
