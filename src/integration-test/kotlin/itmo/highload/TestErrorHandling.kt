package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.AnimalDto
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.utils.defaultJsonRequestSpec
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@IntegrationTestContext
class TestErrorHandling {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test entity not found exception`() {
        val invalidId = 9999

        RestAssured.get("/api/v1/animals/$invalidId")
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body(equalTo("Animal with ID $invalidId not found"))
    }

    @Test
    fun `should return BAD_REQUEST when delete non-pending adoption-request`() {
        val animalId = 2
        val requestId = defaultJsonRequestSpec().post("/api/v1/adoptions/$animalId")
            .then().statusCode(HttpStatus.CREATED.value()).extract().path<Int>("id")

        defaultJsonRequestSpec().body(UpdateAdoptionRequestStatusDto(id = requestId, AdoptionStatus.APPROVED))
            .patch("/api/v1/adoptions")
            .then().statusCode(HttpStatus.OK.value())

        RestAssured.delete("/api/v1/adoptions/$animalId")
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body(equalTo("Cannot delete adoption request with status: APPROVED"))
    }

    @Test
    fun `test invalid animal update exception`() {
        val animalId = 1
        val invalidUpdateDto = AnimalDto(
            name = "Updated Animal",
            type = "Cat",
            gender = Gender.FEMALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        defaultJsonRequestSpec().body(invalidUpdateDto).put("/api/v1/animals/$animalId")
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body(equalTo("Can't change gender; Can't change type of animal; Can't cancel castration of an animal"))
    }
}