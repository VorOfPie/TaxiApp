package com.modsen.taxi.driversrvice.driver;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ActiveProfiles("test")
public abstract class BaseContractTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("/realm-export.json");
    private WebTestClient client;
    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configurerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

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

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        KEYCLOAK.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
        KEYCLOAK.start();
    }

    @BeforeEach
    public void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        RestAssuredWebTestClient.webTestClient(client);
    }
}
