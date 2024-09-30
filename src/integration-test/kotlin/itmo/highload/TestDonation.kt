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
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@IntegrationTestContext
class TestDonation @Autowired constructor(
    private val transactionRepository: TransactionRepository,
) {
    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/transactions/donations"

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
            purposeId = balance.id,
            moneyAmount = transactionMoney
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(apiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("money_amount", equalTo(transactionMoney))
    }

    @Test
    fun `test get all donations for customer`() {
        val expectedTransactionResponse = transactionRepository.findAll().filter { it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val response: Response = defaultJsonRequestSpec().get(apiUrlBasePath)
        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value())
            .body("", `is`(expectedTransactionResponse))
    }

    @Test
    fun `test get all donations for expense manager`() {
        val expectedTransactionResponse = transactionRepository.findAll().filter { it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val response = defaultJsonRequestSpec().get(apiUrlBasePath).then().statusCode(HttpStatus.OK.value()).extract()
            .`as`(Array<TransactionResponse>::class.java)

        assertEquals(expectedTransactionResponse.size, response.size)
        for (i in expectedTransactionResponse.indices) {
            assertEquals(expectedTransactionResponse[i], response[i])
        }
    }
}
