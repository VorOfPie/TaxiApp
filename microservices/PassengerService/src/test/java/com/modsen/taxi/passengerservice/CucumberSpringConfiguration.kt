package com.modsen.taxi.passengerservice;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@SpringBootTest(classes = PassengerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CucumberSpringConfiguration {

    static PostgreSQLContainer postgresContainer;

    @BeforeAll
    public static void setup() {
        System.out.println("starting DB");
        DockerImageName myImage = DockerImageName.parse("postgres:16")
                .asCompatibleSubstituteFor("postgres");
        postgresContainer = new PostgreSQLContainer(myImage)
                .withDatabaseName("passenger_db")
                .withUsername("username")
                .withPassword("password");
        postgresContainer.start();
        System.out.println(postgresContainer.getJdbcUrl());
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("closing DB connection");
        postgresContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
}
