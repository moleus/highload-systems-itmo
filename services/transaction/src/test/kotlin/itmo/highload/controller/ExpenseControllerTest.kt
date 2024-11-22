package itmo.highload.controller

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.TransactionService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.LocalDateTime

class ExpenseControllerTest {

    private lateinit var controller: ExpenseController
    private lateinit var transactionService: TransactionService
    private lateinit var jwtUtils: JwtUtils

    @BeforeEach
    fun setUp() {
        transactionService = mockk()
        jwtUtils = mockk()
        controller = ExpenseController(transactionService, jwtUtils)
    }

    @Test
    fun `getExpenses - should return all expenses`() {
        val purposeId = 1
        val expenses = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 1, name = "Медицина"), userId = 1, moneyAmount = 500, isDonation = false
            ),
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 2, name = "Питание"), userId = 2, moneyAmount = 100, isDonation = false
            ),
        )

        every { transactionService.getExpenses(purposeId) } returns Flux.fromIterable(expenses)

        StepVerifier.create(controller.getExpenses(purposeId))
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(2024, 12, 1, 12, 0) &&
                        it.purpose.id == 1 && it.moneyAmount == 500 && it.userId == 1 && !it.isDonation
            }
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(2024, 12, 1, 12, 0) &&
                        it.purpose.id == 2 && it.moneyAmount == 100 && it.userId == 2 && !it.isDonation
            }
            .verifyComplete()
    }

    @Test
    fun `addExpense - should add a new expense`() {
        val expenseDto = TransactionDto(moneyAmount = 300, purposeId = 2)
        val expenseResponse = TransactionResponse(
            dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
            PurposeResponse(id = 2, name = "Питание"), userId = 1, moneyAmount = 300, isDonation = false
        )
        val token = "validToken"
        val userId = 1

        every { jwtUtils.extractUserId(token) } returns userId
        every { transactionService.addTransaction(expenseDto, userId, isDonation = false) } returns
                Mono.just(expenseResponse)

        StepVerifier.create(controller.addExpense(expenseDto, token))
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(2024, 12, 1, 12, 0) &&
                        it.purpose.id == 2 && it.purpose.name == "Питание" &&
                        it.userId == 1 && it.moneyAmount == 300 && !it.isDonation
            }
            .verifyComplete()
    }

    @Test
    fun `addExpense - should return 400 if invalid data provided`() {
        val expenseDto = TransactionDto(moneyAmount = -300, purposeId = 2)  // Неверная сумма
        val token = "validToken"
        val userId = 1

        every { jwtUtils.extractUserId(token) } returns userId
        every { transactionService.addTransaction(expenseDto, userId, isDonation = false) } returns
                Mono.error(IllegalArgumentException("Invalid expense amount"))

        StepVerifier.create(controller.addExpense(expenseDto, token))
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }
}
