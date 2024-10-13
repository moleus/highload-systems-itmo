import io.r2dbc.spi.ConnectionFactory
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.fixtures.TransactionResponseFixture
import itmo.highload.utils.defaultJsonRequestSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@IntegrationTestContext
@Sql("/test-data.sql")
class TestTransactions {
    @LocalServerPort
    private var port: Int = 0
    private val donationApiUrlBasePath = "/api/v1/transactions/donations"
    private val expenseApiUrlBasePath = "/api/v1/transactions/expenses"

    companion object {
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15"))
            .apply {
                this.withDatabaseName("testDb")
                    .withUsername("root")
                    .withPassword("123456")
                    .withReuse(true)
            }

        @JvmStatic
        @BeforeAll
        internal fun setUp(): Unit {
            postgres.start()
        }
    }

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @BeforeEach
    fun populateTestData(
        @Value("classpath:test-data.sql") testDataSql: Resource,
        connectionFactory: ConnectionFactory
    ) {
        val resourceDatabasePopulator = ResourceDatabasePopulator()
        resourceDatabasePopulator.addScript(testDataSql)
        resourceDatabasePopulator.populate(connectionFactory).block()
    }

    @Test
    fun `test add donation`() {
        @Suppress("MagicNumber")
        val transactionMoney = 200
        val transactionDto = TransactionDto(
            purposeId = 1, moneyAmount = transactionMoney
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(donationApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `test get all donations`() {
        val expectedTransactionResponse = listOf(
            TransactionResponseFixture.of()
        )

        val actualTransactionResponse = defaultJsonRequestSpec().get(donationApiUrlBasePath)
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(Array<TransactionResponse>::class.java).toList()

        assertThat(actualTransactionResponse).containsExactlyInAnyOrderElementsOf(expectedTransactionResponse)
    }

    @Test
    fun `test add expense`() {
        @Suppress("MagicNumber")
        val transactionMoney = 300
        val transactionDto = TransactionDto(
            purposeId = 1, moneyAmount = transactionMoney
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `should return BAD_REQUEST when adding donation with negative money amount`() {
        val transactionDto = TransactionDto(
            purposeId = 1, moneyAmount = -200
        )

        defaultJsonRequestSpec().body(transactionDto).post(donationApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return BAD_REQUEST when adding expense with negative money amount`() {
        val transactionDto = TransactionDto(
            purposeId = 1, moneyAmount = -200
        )

        defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return BAD_REQUEST when there is not enough money on balance`() {
        val transactionDto = TransactionDto(purposeId = 1, moneyAmount = 1001)

        defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `test get all expenses`() {
        val expectedTransactionResponse = listOf(
            TransactionResponseFixture.of(isDonation = false)
        )

        val actualTransactionResponse = defaultJsonRequestSpec().get(expenseApiUrlBasePath)
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(Array<TransactionResponse>::class.java).toList()

        assertThat(actualTransactionResponse).containsExactlyInAnyOrderElementsOf(expectedTransactionResponse)
    }
}
