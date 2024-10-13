package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [itmo.highload.AnimalServiceApplication::class]
)
@ActiveProfiles(profiles=["disable-security", "test"])
//@ComponentScan("itmo.highload")
@AutoConfigureWebMvc
@Sql("/test-data.sql")
@Target(AnnotationTarget.CLASS)
annotation class IntegrationTestContext
