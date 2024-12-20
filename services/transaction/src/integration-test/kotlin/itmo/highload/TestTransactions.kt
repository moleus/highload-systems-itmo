package itmo.highload

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.TransactionDto
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.kafka.KafkaContainer
import reactor.core.publisher.Mono

@R2dbcIntegrationTestContext
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestTransactions @Autowired constructor(
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

        @Container
        @Suppress("UnusedPrivateProperty")
        private val hazelcast = GenericContainer<Nothing>("hazelcast/hazelcast:5-jdk21").apply {
            this.withExposedPorts(5701)
        }

        @DynamicPropertySource
        @JvmStatic
        fun hazelcastProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.hazelcast.config") { "classpath:hazelcast-client.yaml" }
        }
    }

    @LocalServerPort
    private var port: Int = 0
    private val donationApiUrlBasePath = "/api/v1/transactions/donations"
    private val expenseApiUrlBasePath = "/api/v1/transactions/expenses"

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
    fun rollOutTestData(@Value("classpath:/changelog-test/test-data.sql") script: Resource) {
        executeScriptBlocking(script)
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test add donation`() {
        val transactionMoney = 200
        val transactionDto = TransactionDto(
            purposeId = -1, moneyAmount = transactionMoney
        )

        val response = defaultJsonRequestSpec().withJwt(customerToken).body(transactionDto).post(donationApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `test add expense`() {
        val transactionMoney = 300
        val transactionDto = TransactionDto(
            purposeId = -1, moneyAmount = transactionMoney
        )

        val response = defaultJsonRequestSpec().withJwt(managerToken).body(transactionDto).post(expenseApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `should return BAD_REQUEST when adding donation with negative money amount`() {
        val transactionDto = TransactionDto(
            purposeId = -1, moneyAmount = -200
        )

        defaultJsonRequestSpec().withJwt(customerToken).body(transactionDto).post(donationApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return BAD_REQUEST when adding expense with negative money amount`() {
        val transactionDto = TransactionDto(
            purposeId = -1, moneyAmount = -200
        )

        defaultJsonRequestSpec().withJwt(managerToken).body(transactionDto).post(expenseApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }
}
