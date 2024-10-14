package itmo.highload.configuration

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [itmo.highload.AdoptionServiceApplication::class,
    ]
//    properties = ["server.port=9999", "spring.datasource.url=jdbc:tc:postgresql:15:///test?TC_TMPFS=/testtmpfs:rw"]
)
@ActiveProfiles(profiles=["disable-security", "test"])
@AutoConfigureWebMvc
@Sql("/test-data.sql")
//@TestPropertySource(value = ["classpath:application.yml"])
@Target(AnnotationTarget.CLASS)
annotation class IntegrationTestContext
