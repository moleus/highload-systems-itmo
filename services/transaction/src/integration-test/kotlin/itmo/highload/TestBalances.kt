package itmo.highload

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.model.BalanceMapper
import itmo.highload.repository.BalanceRepository
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.assertj.core.api.Assertions.assertThat
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
class TestBalances @Autowired constructor(
    private val connectionFactory: ConnectionFactory,
    private val balanceRepository: BalanceRepository,
    jwtUtils: JwtUtils
) {

    @LocalServerPort
    private var port: Int = 0
    private val balanceApiUrlBasePath = "/api/v1/balances"

    private val managerToken = jwtUtils.generateAccessToken(
        "emanager",
        Role.EXPENSE_MANAGER,
        -3
    )

    private val customerToken = jwtUtils.generateAccessToken(
        "customer",
        Role.CUSTOMER,
        -2
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
    fun `test get all balances`() {
        val expectedBalanceResponse = balanceRepository.findAll().collectList().block()!!.map {
            BalanceResponse(
                purpose = BalanceMapper.toPurposeResponse(it), moneyAmount = it.moneyAmount
            )
        }

        val actualBalanceResponse =
            defaultJsonRequestSpec().withJwt(managerToken).get(balanceApiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(Array<BalanceResponse>::class.java).toList()

        assertThat(actualBalanceResponse).containsExactlyInAnyOrderElementsOf(expectedBalanceResponse)
    }

    @Test
    fun `test get balance by id`() {
        val balance = balanceRepository.findAll().blockFirst()!!
        val expectedBalanceResponse = BalanceResponse(
            purpose = BalanceMapper.toPurposeResponse(balance), moneyAmount = balance.moneyAmount
        )

        val actualBalanceResponse = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/${balance.id}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(BalanceResponse::class.java)

        assertThat(actualBalanceResponse).isEqualTo(expectedBalanceResponse)
    }

    @Test
    fun `test get all purposes`() {
        val expectedPurposeResponse =
            balanceRepository.findAll().collectList().block()!!.map { BalanceMapper.toPurposeResponse(it) }

        val actualPurposeResponse = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(actualPurposeResponse).containsExactlyInAnyOrderElementsOf(expectedPurposeResponse)
    }

    @Test
    fun `test add purpose`() {
        val newPurpose = "New Purpose"

        val allPurposes = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(allPurposes).allSatisfy { assertThat(it.name).isNotEqualTo(newPurpose) }
        val initialPurposeSize = allPurposes.size

        defaultJsonRequestSpec().withJwt(managerToken).body(newPurpose).post("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo(newPurpose))

        val updatedPurposes = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(updatedPurposes).hasSize(initialPurposeSize + 1)
        assertThat(updatedPurposes).anyMatch { it.name == newPurpose }
    }
}
