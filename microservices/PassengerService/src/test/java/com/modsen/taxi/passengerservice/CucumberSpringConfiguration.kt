package com.modsen.taxi.passengerservice

import io.cucumber.java.AfterAll
import io.cucumber.java.BeforeAll
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@CucumberContextConfiguration
@SpringBootTest(classes = [PassengerServiceApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CucumberSpringConfiguration {

    companion object {
        private var postgresContainer: PostgreSQLContainer<*>? = null

        @BeforeAll
        @JvmStatic
        fun setup() {
            println("Starting DB")
            val myImage = DockerImageName.parse("postgres:16")
                .asCompatibleSubstituteFor("postgres")
            postgresContainer = PostgreSQLContainer(myImage)
                .withDatabaseName("passenger_db")
                .withUsername("username")
                .withPassword("password")
            postgresContainer?.start()
            println(postgresContainer?.jdbcUrl)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            println("Closing DB connection")
            postgresContainer?.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer?.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer?.username }
            registry.add("spring.datasource.password") { postgresContainer?.password }
        }
    }
}
