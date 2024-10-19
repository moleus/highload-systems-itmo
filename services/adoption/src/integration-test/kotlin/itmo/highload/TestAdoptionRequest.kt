@file:Suppress("MagicNumber")

package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.configuration.JdbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.fixtures.AdoptionRequestResponseFixture
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@JdbcIntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    jwtUtils: JwtUtils,
) : TestContainerIntegrationTest() {
    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoptions"

    private val adoptionManagerToken = jwtUtils.generateAccessToken(
        "amanager",
        Role.ADOPTION_MANAGER,
        -4
    )

    private val customerToken = jwtUtils.generateAccessToken(
        "customer",
        Role.CUSTOMER,
        -2
    )

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test add adoption request`() {
        val expectedAdoptionRequestResponse = AdoptionRequestResponseFixture.of(
            dateTime = LocalDateTime.now(), status = AdoptionStatus.PENDING, customerId = -2, animalId = -2
        )

        val response = defaultJsonRequestSpec().withJwt(customerToken)
            .post("$apiUrlBasePath/${expectedAdoptionRequestResponse.animalId}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value()).extract().body()
            .`as`(AdoptionRequestResponse::class.java)

        assertThat(response.status).isEqualTo(expectedAdoptionRequestResponse.status)
        assertThat(response.customerId).isEqualTo(expectedAdoptionRequestResponse.customerId)
        assertThat(response.animalId).isEqualTo(expectedAdoptionRequestResponse.animalId)

        val expectedMessage =
            "An adoption request already exists for customer ID: ${expectedAdoptionRequestResponse.customerId} " +
                    "and animal ID: ${expectedAdoptionRequestResponse.animalId}"
        defaultJsonRequestSpec().withJwt(customerToken)
            .post("$apiUrlBasePath/${expectedAdoptionRequestResponse.animalId}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CONFLICT.value()).body(`is`(expectedMessage))
    }

    @Test
    fun `test get all adoption requests for customer`() {
        val expectedAdoptionRequestResponse = listOf(
            AdoptionRequestResponseFixture.of(
                id = -1,
                dateTime = LocalDateTime.parse("2023-01-01T00:00:00"),
                status = AdoptionStatus.PENDING,
                customerId = -2,
                animalId = -1
            )
        )

        val result = defaultJsonRequestSpec().withJwt(adoptionManagerToken).get(apiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract().body()
            .`as`(Array<AdoptionRequestResponse>::class.java).toList()

        assertThat(result).isEqualTo(expectedAdoptionRequestResponse)
    }

    @Test
    fun `should return list of statuses`() {
        val expectedStatuses = listOf(AdoptionStatus.PENDING, AdoptionStatus.APPROVED, AdoptionStatus.DENIED)
        val response =
            defaultJsonRequestSpec().withJwt(customerToken).get("$apiUrlBasePath/statuses").then().log()
                .ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().body().`as`(Array<AdoptionStatus>::class.java).toList()

        assertThat(response).containsExactlyInAnyOrderElementsOf(expectedStatuses)
    }

    @Test
    fun `should return BAD_REQUEST when delete non-pending adoption-request`() {
        @Suppress("MagicNumber")
        val animalId = -2
        val requestId =
            defaultJsonRequestSpec().withJwt(customerToken).post("/api/v1/adoptions/$animalId").then().log()
                .ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.CREATED.value()).extract().path<Int>("id")

        defaultJsonRequestSpec().withJwt(adoptionManagerToken)
            .body(UpdateAdoptionRequestStatusDto(id = requestId, AdoptionStatus.APPROVED))
            .patch("/api/v1/adoptions").then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value())

        defaultJsonRequestSpec().withJwt(customerToken).delete("/api/v1/adoptions/$animalId").then().log()
            .ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body(CoreMatchers.equalTo("Cannot delete adoption request with status: APPROVED"))
    }
}
