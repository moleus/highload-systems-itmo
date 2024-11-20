package itmo.highload

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.fixtures.BalanceResponseFixture
import itmo.highload.fixtures.PurposeResponseFixture
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.kafka.KafkaContainer
import reactor.core.publisher.Mono

@R2dbcIntegrationTestContext
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestBalance @Autowired constructor(
    private val connectionFactory: ConnectionFactory,
    jwtUtils: JwtUtils
) : TestContainerIntegrationTest() {
    companion object {
        @Container
        @Suppress("UnusedPrivateProperty")
        private val kafka = KafkaContainer("apache/kafka-native:3.8.0").apply {
            this.withTmpFs(mapOf("/testtmpfs" to "rw"))
        }

        @DynamicPropertySource
        @JvmStatic
        fun kafkaProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
        }
    }

    @LocalServerPort
    private var port: Int = 0
    private val balanceApiUrlBasePath = "/api/v1/balances"

    private val managerToken = jwtUtils.generateAccessToken(
        "emanager",
        Role.EXPENSE_MANAGER,
        -3
    )

    private fun executeScriptBlocking(sqlScript: Resource) {
        Mono.from(connectionFactory.create())
            .flatMap<Any> { connection: Connection -> ScriptUtils.executeSqlScript(connection, sqlScript) }.block()
    }

    @BeforeEach
    fun rollOutTestData(@Value("classpath:/changelog-test/test-data.sql") script: Resource) {
        executeScriptBlocking(script)
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test get all balances`() {
        val expectedBalanceResponse = listOf(BalanceResponseFixture.of())

        val actualBalanceResponse =
            defaultJsonRequestSpec().withJwt(managerToken).get(balanceApiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(Array<BalanceResponse>::class.java).toList()

        assertThat(actualBalanceResponse).hasSize(3)
        assertThat(expectedBalanceResponse).containsAnyElementsOf(actualBalanceResponse)
    }

    @Test
    fun `test get balance by id`() {
        val expectedBalanceResponse = BalanceResponseFixture.of()

        val actualBalanceResponse = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/-1").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(BalanceResponse::class.java)

        assertThat(actualBalanceResponse).isEqualTo(expectedBalanceResponse)
    }

    @Test
    fun `test get all purposes`() {
        val expectedPurposeResponse = listOf(PurposeResponseFixture.of())

        val actualPurposeResponse = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(actualPurposeResponse).hasSize(3)
        assertThat(expectedPurposeResponse).containsAnyElementsOf(actualPurposeResponse)
    }

    @Test
    fun `test add purpose`() {
        val newPurpose = PurposeRequestDto(name = "New Purpose")

        val allPurposes = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(allPurposes).allSatisfy { assertThat(it.name).isNotEqualTo(newPurpose.name) }
        val initialPurposeSize = allPurposes.size

        defaultJsonRequestSpec().withJwt(managerToken).body(newPurpose).post("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo(newPurpose.name))

        val updatedPurposes = defaultJsonRequestSpec().withJwt(managerToken).get("$balanceApiUrlBasePath/purposes").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<PurposeResponse>::class.java).toList()

        assertThat(updatedPurposes).hasSize(initialPurposeSize + 1)
        assertThat(updatedPurposes).anyMatch { it.name == newPurpose.name }
    }
}
