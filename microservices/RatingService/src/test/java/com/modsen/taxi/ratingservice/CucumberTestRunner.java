package com.modsen.taxi.ratingservice;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.modsen.taxi.ratingservice"
)
public class CucumberTestRunner {
}
