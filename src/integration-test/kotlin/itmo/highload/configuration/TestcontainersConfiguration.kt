package itmo.highload.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        val container = PostgreSQLContainer(DockerImageName.parse("postgres:15"))
        container.start()
        System.setProperty("SPRING_DATASOURCE_URL", container.jdbcUrl)
        System.setProperty("SPRING_DATASOURCE_USERNAME", container.username)
        System.setProperty("SPRING_DATASOURCE_PASSWORD", container.password)
        return container
    }
}