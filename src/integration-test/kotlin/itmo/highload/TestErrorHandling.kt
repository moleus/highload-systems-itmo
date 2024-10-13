package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.AnimalDto
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AnimalRepository
import itmo.highload.utils.defaultJsonRequestSpec
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus


@IntegrationTestContext
class TestErrorHandling @Autowired constructor(
    private val animalRepository: AnimalRepository
){

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test entity not found exception`() {
        @Suppress("MagicNumber")
        val invalidId = 9999

        RestAssured.get("/api/v1/animals/$invalidId")
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body(equalTo("Animal with ID $invalidId not found"))
    }

}
