package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integration-test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebMvc
@TestPropertySource(value = ["classpath:application-test.properties"])
@Target(AnnotationTarget.CLASS)
annotation class IntegrationTestContext
