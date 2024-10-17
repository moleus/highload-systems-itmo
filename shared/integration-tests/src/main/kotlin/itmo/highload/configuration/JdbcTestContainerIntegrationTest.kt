package itmo.highload.configuration

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration


@Testcontainers
@Suppress("UtilityClassWithPublicConstructor")
abstract class JdbcTestContainerIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15")).apply {
            this.withDatabaseName("test").withUsername("test").withPassword("test").withReuse(true)
                .withTmpFs(mapOf("/testtmpfs" to "rw")).withMinimumRunningDuration(Duration.ofSeconds(5L));
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.username", postgres::getUsername);
        }
    }
}
