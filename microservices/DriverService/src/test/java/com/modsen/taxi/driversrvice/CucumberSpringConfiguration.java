package com.modsen.taxi.driversrvice;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = DriverServiceApplication.class)
public class CucumberSpringConfiguration {
}