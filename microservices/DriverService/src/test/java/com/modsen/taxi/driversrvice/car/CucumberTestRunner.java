package com.modsen.taxi.driversrvice.car;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/car-controller.feature",
        glue = "com.modsen.taxi.driversrvice.car"
)
public class CucumberTestRunner {
}
