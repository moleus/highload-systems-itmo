package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql

@ActiveProfiles("integration-test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebMvc
@Sql("/test-data.sql")
@TestPropertySource(value = ["classpath:application-test.properties"])
@Target(AnnotationTarget.CLASS)
annotation class IntegrationTestContext
