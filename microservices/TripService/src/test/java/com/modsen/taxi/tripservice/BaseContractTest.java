package com.modsen.taxi.tripservice;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(ids = {"com.modsen.taxi:PassengerService:+:stubs:7001",
                "com.modsen.taxi:DriverService:+:stubs:7002"},
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class BaseContractTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));
    @Autowired
    private MockMvc mockMvc;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }


    @Test
    public void testCreateTrip_success() throws Exception {
        String tripRequestJson = """
                {
                    "driverId": 1,
                    "passengerId": 1,
                    "originAddress": "123 Main St",
                    "destinationAddress": "456 Elm St",
                    "status": "CREATED",
                    "orderDateTime": "2024-10-17T10:00:00",
                    "price": 25.50
                }
                """;

        mockMvc.perform(post("/api/v1/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.passengerId", is(1)))
                .andExpect(jsonPath("$.originAddress", is("123 Main St")))
                .andExpect(jsonPath("$.destinationAddress", is("456 Elm St")))
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.price", is(25.50)));
    }

    @Test
    public void testCreateTrip_driverNotFound() throws Exception {
        String tripRequestJson = """
                {
                    "driverId": 999,
                    "passengerId": 1,
                    "originAddress": "123 Main St",
                    "destinationAddress": "456 Elm St",
                    "status": "CREATED",
                    "orderDateTime": "2024-10-17T10:00:00",
                    "price": 25.50
                }
                """;

        mockMvc.perform(post("/api/v1/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Driver with id 999 not found")));
    }

    @Test
    public void testCreateTrip_passengerNotFound() throws Exception {
        String tripRequestJson = """
                {
                    "driverId": 1,
                    "passengerId": 999,
                    "originAddress": "123 Main St",
                    "destinationAddress": "456 Elm St",
                    "status": "CREATED",
                    "orderDateTime": "2024-10-17T10:00:00",
                    "price": 25.50
                }
                """;

        mockMvc.perform(post("/api/v1/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Passenger with id 999 not found")));
    }
}
