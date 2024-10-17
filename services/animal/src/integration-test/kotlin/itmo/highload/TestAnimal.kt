package itmo.highload

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.configuration.JdbcTestContainerIntegrationTest
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.fixtures.AnimalResponseFixture
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.connection.init.ScriptUtils
import reactor.core.publisher.Mono

@R2dbcIntegrationTestContext
class TestAnimal @Autowired constructor(
    private val connectionFactory: ConnectionFactory, jwtUtils: JwtUtils
) : JdbcTestContainerIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0
    private val animalApiUrlBasePath = "/api/v1/animals"

    private val adoptionManagerToken = jwtUtils.generateAccessToken(
        "amanager", Role.ADOPTION_MANAGER, -4
    )

    private val customerToken = jwtUtils.generateAccessToken(
        "customer", Role.CUSTOMER, -2
    )

    private fun executeScriptBlocking(sqlScript: Resource) {
        Mono.from(connectionFactory.create())
            .flatMap<Any> { connection: Connection -> ScriptUtils.executeSqlScript(connection, sqlScript) }.block()
    }

    @BeforeEach
    fun rollOutTestData(@Value("classpath:/test-data.sql") script: Resource) {
        executeScriptBlocking(script)
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test get all animals`() {
        val expectedAnimalResponse = listOf(
            AnimalResponseFixture.of(
                id = -1,
                name = "Buddy",
                type = "Dog",
                gender = Gender.MALE,
                isCastrated = true,
                healthStatus = HealthStatus.HEALTHY
            ), AnimalResponseFixture.of(
                id = -2,
                name = "Molly",
                type = "Cat",
                gender = Gender.FEMALE,
                isCastrated = false,
                healthStatus = HealthStatus.SICK
            )
        )

        val actualAnimalResponse =
            defaultJsonRequestSpec().withJwt(customerToken).get(animalApiUrlBasePath).then().log()
                .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
                .`as`(Array<AnimalResponse>::class.java).toList()

        assertThat(actualAnimalResponse).containsExactlyInAnyOrderElementsOf(expectedAnimalResponse)
    }

    @Test
    fun `test get animal by id`() {
        val expectedAnimalResponse = AnimalResponseFixture.of()

        val actualAnimalResponse =
            defaultJsonRequestSpec().withJwt(customerToken).get("$animalApiUrlBasePath/-1").then().log()
                .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
                .`as`(AnimalResponse::class.java)

        assertThat(actualAnimalResponse).isEqualTo(expectedAnimalResponse)
    }

    @Test
    fun `test add animal`() {
        val animalDto = AnimalDto(
            name = "New Animal",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val response = defaultJsonRequestSpec().withJwt(adoptionManagerToken).body(animalDto).post(animalApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo(animalDto.name))
    }

    @Test
    fun `test update animal`() {
        val updatedAnimalDto = AnimalDto(
            name = "Updated Animal",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        defaultJsonRequestSpec().withJwt(adoptionManagerToken).body(updatedAnimalDto).put("$animalApiUrlBasePath/-1")
            .then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value())
            .body("name", equalTo(updatedAnimalDto.name))
    }

    @Test
    fun `test delete animal`() {
        defaultJsonRequestSpec().withJwt(adoptionManagerToken).delete("$animalApiUrlBasePath/-1").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `test invalid animal update exception`() {
        val invalidUpdateDto = AnimalDto(
            name = "Updated Animal",
            type = "Cat",
            gender = Gender.FEMALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        defaultJsonRequestSpec().withJwt(adoptionManagerToken).body(invalidUpdateDto).put("$animalApiUrlBasePath/-1")
            .then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value()).body(
                CoreMatchers.equalTo(
                    "Can't change gender; Can't change type of animal; " + "Can't cancel castration of an animal"
                )
            )
    }
}
