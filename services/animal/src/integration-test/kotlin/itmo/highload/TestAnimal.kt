package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.configuration.JdbcTestContainerIntegrationTest
import itmo.highload.fixtures.AnimalResponseFixture
import itmo.highload.utils.defaultJsonRequestSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@IntegrationTestContext
class TestAnimal : JdbcTestContainerIntegrationTest() {
    @LocalServerPort
    private var port: Int = 0
    private val animalApiUrlBasePath = "/api/v1/animals"


    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test get all animals`() {
        val expectedAnimalResponse = listOf(
            AnimalResponseFixture.of(
                id = 1,
                name = "Buddy",
                type = "Dog",
                gender = Gender.MALE,
                isCastrated = true,
                healthStatus = HealthStatus.HEALTHY
            ), AnimalResponseFixture.of(
                id = 2,
                name = "Molly",
                type = "Cat",
                gender = Gender.FEMALE,
                isCastrated = false,
                healthStatus = HealthStatus.SICK
            )
        )

        val actualAnimalResponse =
            defaultJsonRequestSpec().get(animalApiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(Array<AnimalResponse>::class.java).toList()

        assertThat(actualAnimalResponse).containsExactlyInAnyOrderElementsOf(expectedAnimalResponse)
    }

    @Test
    fun `test get animal by id`() {
        val expectedAnimalResponse = AnimalResponseFixture.of(
            id = 1,
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val actualAnimalResponse =
            defaultJsonRequestSpec().get("$animalApiUrlBasePath/1").then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(AnimalResponse::class.java)

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

        val response: Response = defaultJsonRequestSpec().body(animalDto).post(animalApiUrlBasePath)

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

        defaultJsonRequestSpec().body(updatedAnimalDto).put("$animalApiUrlBasePath/1").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value())
            .body("name", equalTo(updatedAnimalDto.name))
    }

    @Test
    fun `test delete animal`() {
        defaultJsonRequestSpec().delete("$animalApiUrlBasePath/1").then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.NO_CONTENT.value())
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

        defaultJsonRequestSpec().body(invalidUpdateDto).put("$animalApiUrlBasePath/1").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value()).body(
                CoreMatchers.equalTo(
                    "Can't change gender; Can't change type of animal; " + "Can't cancel castration of an animal"
                )
            )
    }
}
