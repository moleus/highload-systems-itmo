package itmo.highload

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.dto.response.UserResponse
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.enum.UserRole
import itmo.highload.repository.TransactionRepository
import itmo.highload.repository.UserRepository
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.time.LocalDateTime

val balances = listOf(
    Balance(
        id = 1,
        purpose = "Medicine",
        moneyAmount = 1000
    ),
    Balance(
        id = 2,
        purpose = "Food",
        moneyAmount = 500
    ),
    Balance(
        id = 3,
        purpose = "General",
        moneyAmount = 0
    )
)

val transactions = listOf(
    Transaction(
        id = 1000,
        dateTime = LocalDateTime.now(),
        moneyAmount = 100,
        isDonation = true,
        user = users[0],
        balance = balances[0],
    ),
    Transaction(
        id = 1001,
        dateTime = LocalDateTime.now(),
        moneyAmount = 200,
        isDonation = false,
        user = users[0],
        balance = balances[1],
    ),
)

@IntegrationTestContext
class TestDonation @Autowired constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
) {

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/transactions/donations"

    private fun getCustomerToken(login: String) = jwtProvider.generateAccessToken(login, UserRole.CUSTOMER)

    private fun getExpenseManagerToken(login: String) =
        jwtProvider.generateAccessToken(login, UserRole.EXPENSE_MANAGER)

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON

        userRepository.deleteAll()
        userRepository.saveAll(transactions.map { it.user })
        transactionRepository.deleteAll()
        transactionRepository.saveAll(transactions)
    }

    @Test
    fun `test add donation`() {
        val user = transactions[0].user
        val token = getCustomerToken(user.login)
        val balance = balances[0]

        val transactionDto = TransactionDto(
            purposeId = balance.id,
            moneyAmount = 200
        )

        val response: Response = defaultJsonRequestSpec().withJwt(token).body(transactionDto).post(apiUrlBasePath)

        response.then().statusCode(201).body("moneyAmount", equalTo(200))
    }

    @Test
    fun `test get all donations for customer`() {
        val user = transactions[0].user
        val token = getCustomerToken(user.login)

        val expectedTransactionResponse = transactions.filter { it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val response: Response = defaultJsonRequestSpec().withJwt(token).get(apiUrlBasePath)
        response.then().statusCode(200).body("", `is`(expectedTransactionResponse))
    }

    @Test
    fun `test get all donations for expense manager`() {
        val token = getExpenseManagerToken("manager")

        val expectedTransactionResponse = transactions.filter { it.isDonation }.map {
            TransactionResponse(
                dateTime = it.dateTime,
                purpose = PurposeResponse(it.balance.id, it.balance.purpose),
                user = UserResponse(it.user.id, it.user.login),
                moneyAmount = it.moneyAmount,
                isDonation = it.isDonation
            )
        }

        val response: Response = defaultJsonRequestSpec().withJwt(token).get(apiUrlBasePath)
        response.then().statusCode(200).body("", `is`(expectedTransactionResponse))
    }
}