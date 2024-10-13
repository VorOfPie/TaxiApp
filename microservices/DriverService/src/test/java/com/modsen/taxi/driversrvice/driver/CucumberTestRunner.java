package com.modsen.taxi.driversrvice.driver;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/driver-controller.feature",
        glue = "com.modsen.taxi.driversrvice.driver"
)
public class CucumberTestRunner {
}
