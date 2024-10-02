package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.dto.response.UserResponse
import itmo.highload.repository.BalanceRepository
import itmo.highload.repository.TransactionRepository
import itmo.highload.utils.defaultJsonRequestSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@IntegrationTestContext
class TestTransactions @Autowired constructor(
    private val transactionRepository: TransactionRepository, private val balanceRepository: BalanceRepository
) {
    @LocalServerPort
    private var port: Int = 0
    private val donationApiUrlBasePath = "/api/v1/transactions/donations"
    private val expenseApiUrlBasePath = "/api/v1/transactions/expenses"

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test add donation`() {
        val balance = balanceRepository.findAll().first()
        @Suppress("MagicNumber")
        val transactionMoney = 200

        val transactionDto = TransactionDto(
            purposeId = balance.id, moneyAmount = transactionMoney
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(donationApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `test get all donations`() {
        val expectedTransactionResponse = transactionRepository.findAll().filter { it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val actualTransactionResponse = defaultJsonRequestSpec().get(donationApiUrlBasePath)
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(Array<TransactionResponse>::class.java).toList()

        assertThat(actualTransactionResponse).containsExactlyInAnyOrderElementsOf(expectedTransactionResponse)
    }

    @Test
    fun `test add expense`() {
        val balance = balanceRepository.findAll().first()
        @Suppress("MagicNumber")
        val transactionMoney = 300

        val transactionDto = TransactionDto(
            purposeId = balance.id, moneyAmount = transactionMoney
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `should return BAD_REQUEST when adding donation with negative money amount`() {
        val transactionDto = TransactionDto(purposeId = balanceRepository.findAll().first().id, moneyAmount = -200)

        defaultJsonRequestSpec().body(transactionDto).post(donationApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return BAD_REQUEST when adding expense with negative money amount`() {
        val transactionDto = TransactionDto(purposeId = balanceRepository.findAll().first().id, moneyAmount = -200)

        defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return BAD_REQUEST when there is not enough money on balance`() {
        val balance = balanceRepository.findAll().first()
        val transactionDto = TransactionDto(purposeId = balance.id, moneyAmount = balance.moneyAmount + 1)

        defaultJsonRequestSpec().body(transactionDto).post(expenseApiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `test get all expenses`() {
        val expectedTransactionResponse = transactionRepository.findAll().filter { !it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val actualTransactionResponse = defaultJsonRequestSpec().get(expenseApiUrlBasePath)
            .then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(Array<TransactionResponse>::class.java).toList()

        assertThat(actualTransactionResponse).containsExactlyInAnyOrderElementsOf(expectedTransactionResponse)
    }
}
