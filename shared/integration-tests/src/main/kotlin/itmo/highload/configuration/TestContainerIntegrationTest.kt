package itmo.highload.configuration

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


@Testcontainers
@Suppress("UtilityClassWithPublicConstructor")
abstract class TestContainerIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        @Suppress("UnusedPrivateProperty")
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15")).apply {
            this.withDatabaseName("test")
                .withUsername("test")
                .withPassword("test")
                .withTmpFs(mapOf("/testtmpfs" to "rw"))
                .withReuse(true)
        }
    }
}
