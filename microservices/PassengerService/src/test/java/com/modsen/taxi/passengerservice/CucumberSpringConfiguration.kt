import com.modsen.taxi.passengerservice.PassengerServiceApplication
import dasniko.testcontainers.keycloak.KeycloakContainer
import io.cucumber.java.AfterAll
import io.cucumber.java.BeforeAll
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@CucumberContextConfiguration
@SpringBootTest(
    classes = [PassengerServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class CucumberSpringConfiguration {

    companion object {
        private var postgresContainer: PostgreSQLContainer<*>? = null
        private var keycloak: KeycloakContainer? = null

        @BeforeAll
        @JvmStatic
        fun setup() {
            println("Starting DB and Keycloak")
            val myImage = DockerImageName.parse("postgres:16")
                .asCompatibleSubstituteFor("postgres")
            postgresContainer = PostgreSQLContainer(myImage)
                .withDatabaseName("passenger_db")
                .withUsername("username")
                .withPassword("password")
            postgresContainer?.start()
            println(postgresContainer?.jdbcUrl)

            keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.0")
                .withRealmImportFile("/realm-export.json")
            keycloak?.start()
            println("Keycloak started at: ${keycloak?.authServerUrl}")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            println("Stopping DB and Keycloak")
            keycloak?.stop()
            postgresContainer?.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer?.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer?.username }
            registry.add("spring.datasource.password") { postgresContainer?.password }

            registry.add("keycloak.auth-server-url") { keycloak?.authServerUrl }
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") {
                "${keycloak?.authServerUrl}/realms/taxiapp-realm"
            }
            registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri") {
                "${keycloak?.authServerUrl}/realms/taxiapp-realm"
            }
            registry.add("spring.security.oauth2.client.registration.keycloak.client-id") { "taxiapp" }
            registry.add("spring.security.oauth2.client.registration.keycloak.client-secret") { "Xbfrxu5jJRqzK0C36c0WPOCovoLRerO3" }
        }
    }
}
