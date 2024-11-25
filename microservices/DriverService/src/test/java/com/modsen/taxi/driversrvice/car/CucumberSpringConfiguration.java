package com.modsen.taxi.driversrvice.car;

import com.modsen.taxi.driversrvice.DriverServiceApplication;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@SpringBootTest(classes = DriverServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    static PostgreSQLContainer postgresContainer;
    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("/realm-export.json");

    @BeforeAll
    public static void setup() {
        System.out.println("starting DB");
        DockerImageName myImage = DockerImageName.parse("postgres:16")
                .asCompatibleSubstituteFor("postgres");
        postgresContainer = new PostgreSQLContainer(myImage)
                .withDatabaseName("car_db")
                .withUsername("username")
                .withPassword("password");
        postgresContainer.start();
        KEYCLOAK.start();
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

        registry.add("keycloak.auth-server-url", KEYCLOAK::getAuthServerUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> KEYCLOAK.getAuthServerUrl() + "/realms/taxiapp-realm");
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
                () -> KEYCLOAK.getAuthServerUrl() + "/realms/taxiapp-realm");
        registry.add("spring.security.oauth2.client.registration.keycloak.client-id",
                () -> "taxiapp");
        registry.add("spring.security.oauth2.client.registration.keycloak.client-secret",
                () -> "Xbfrxu5jJRqzK0C36c0WPOCovoLRerO3");

    }
}
