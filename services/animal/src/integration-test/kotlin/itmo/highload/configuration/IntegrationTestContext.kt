package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebMvc
@Sql("/test-data.sql")
@ActiveProfiles("test") // load application-test.yml
@TestPropertySource(value = ["classpath:application-test.yml"])
@Target(AnnotationTarget.CLASS)
annotation class IntegrationTestContext
