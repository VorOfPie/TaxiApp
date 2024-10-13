package com.modsen.taxi.ratingservice;

import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@EmbeddedKafka(partitions = 1, topics = {"rating-topic"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
    static PostgreSQLContainer postgresContainer;
    @MockBean
    private DriverClient driverClient;
    @MockBean
    private PassengerClient passengerClient;

    @BeforeAll
    public static void setup() {
        System.out.println("starting DB");
        DockerImageName myImage = DockerImageName.parse("postgres:16")
                .asCompatibleSubstituteFor("postgres");
        postgresContainer = new PostgreSQLContainer(myImage)
                .withDatabaseName("rating_db")
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
        registry.add("spring.kafka.producer.value-serializer", () -> JsonSerializer.class.getName());
        registry.add("spring.kafka.consumer.value-deserializer", () -> JsonDeserializer.class.getName());
        registry.add("spring.kafka.consumer.group-id", () -> "rating-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");
    }
}
