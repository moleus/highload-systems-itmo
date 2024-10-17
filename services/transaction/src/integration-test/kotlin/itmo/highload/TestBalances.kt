package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.model.BalanceMapper
import itmo.highload.repository.BalanceRepository
import itmo.highload.utils.defaultJsonRequestSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@R2dbcIntegrationTestContext
class TestBalances @Autowired constructor(
    private val balanceRepository: BalanceRepository
) {
    @LocalServerPort
    private var port: Int = 0
    private val balanceApiUrlBasePath = "/api/v1/balances"

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test get all balances`() {
        val expectedBalanceResponse = balanceRepository.findAll().collectList().block()!!.map {
            BalanceResponse(
                purpose = BalanceMapper.toPurposeResponse(it), moneyAmount = it.moneyAmount
            )
        }

        val actualBalanceResponse =
            defaultJsonRequestSpec().get(balanceApiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(Array<BalanceResponse>::class.java).toList()

        assertThat(actualBalanceResponse).containsExactlyInAnyOrderElementsOf(expectedBalanceResponse)
    }

    @Test
    fun `test get balance by id`() {
        val balance = balanceRepository.findAll().blockFirst()!!
        val expectedBalanceResponse = BalanceResponse(
            purpose = BalanceMapper.toPurposeResponse(balance), moneyAmount = balance.moneyAmount
        )

        val actualBalanceResponse = defaultJsonRequestSpec().get("$balanceApiUrlBasePath/${balance.id}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(BalanceResponse::class.java)

        assertThat(actualBalanceResponse).isEqualTo(expectedBalanceResponse)
    }

    @Test
    fun `test get all purposes`() {
        val expectedPurposeResponse =
            balanceRepository.findAll().collectList().block()!!.map { BalanceMapper.toPurposeResponse(it) }

        val actualPurposeResponse = defaultJsonRequestSpec().get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(actualPurposeResponse).containsExactlyInAnyOrderElementsOf(expectedPurposeResponse)
    }

    @Test
    fun `test add purpose`() {
        val newPurpose = "New Purpose"

        val allPurposes = defaultJsonRequestSpec().get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(allPurposes).allSatisfy { assertThat(it.name).isNotEqualTo(newPurpose) }
        val initialPurposeSize = allPurposes.size

        defaultJsonRequestSpec().body(newPurpose).post("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo(newPurpose))

        val updatedPurposes = defaultJsonRequestSpec().get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(updatedPurposes).hasSize(initialPurposeSize + 1)
        assertThat(updatedPurposes).anyMatch { it.name == newPurpose }
    }
}
