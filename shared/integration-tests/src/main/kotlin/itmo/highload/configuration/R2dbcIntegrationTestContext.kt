package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles(profiles=["test"])
@AutoConfigureWebMvc
@Target(AnnotationTarget.CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
annotation class R2dbcIntegrationTestContext
