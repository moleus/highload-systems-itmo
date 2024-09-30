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
import itmo.highload.repository.TransactionRepository
import itmo.highload.repository.UserRepository
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.service.DEMO_CUSTOMER_LOGIN
import itmo.highload.service.DEMO_EXPENSE_MANAGER_LOGIN
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
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

@IntegrationTestContext
class TestDonation @Autowired constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
) {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15")).apply { start() }

        @DynamicPropertySource
        @JvmStatic
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/transactions/donations"

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON

        val transactions = listOf(
            Transaction(
                id = 1000,
                dateTime = LocalDateTime.now(),
                moneyAmount = 100,
                isDonation = true,
                user = userRepository.findByLogin(DEMO_CUSTOMER_LOGIN)!!,
                balance = balances[0],
            ),
            Transaction(
                id = 1001,
                dateTime = LocalDateTime.now(),
                moneyAmount = 200,
                isDonation = false,
                user = userRepository.findByLogin(DEMO_EXPENSE_MANAGER_LOGIN)!!,
                balance = balances[1],
            ),
        )

        transactionRepository.deleteAll()
        transactionRepository.saveAll(transactions)
    }

    @Test
    fun `test add donation`() {
        val balance = balances[0]

        val transactionDto = TransactionDto(
            purposeId = balance.id,
            moneyAmount = 200
        )

        val response: Response = defaultJsonRequestSpec().body(transactionDto).post(apiUrlBasePath)

        response.then().statusCode(201).body("moneyAmount", equalTo(200))
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
        response.then().statusCode(200).body("", `is`(expectedTransactionResponse))
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

        val response: Response = defaultJsonRequestSpec().get(apiUrlBasePath)
        response.then().statusCode(200).body("", `is`(expectedTransactionResponse))
    }
}