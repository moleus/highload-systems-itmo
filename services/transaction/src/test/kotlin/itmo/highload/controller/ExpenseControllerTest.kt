package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.interactor.TransactionService
import itmo.highload.infrastructure.http.ExpenseController
import itmo.highload.security.jwt.JwtUtils
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class ExpenseControllerTest {

    private val transactionService = mockk<TransactionService>()
    private val jwtUtils = mockk<JwtUtils>()
    private val controller = ExpenseController(transactionService, jwtUtils)

    @Test
    fun `getExpenses - should return expenses`() {
        val purposeId = 2
        val token = "validToken"
        val expenses = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(2, "Maintenance"),
                userId = 321,
                moneyAmount = 300,
                isDonation = false,
                status = "SUCCESS"
            ),
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(3, "Utilities"),
                userId = 322,
                moneyAmount = 150,
                isDonation = false,
                status = "PENDING"
            )
        )

        every { transactionService.getExpenses(purposeId, token) } returns Flux.fromIterable(expenses)

        StepVerifier.create(controller.getExpenses(purposeId, token))
            .expectNextMatches {
                it.purpose.name == "Maintenance" && it.moneyAmount == 300 && !it.isDonation && it.status == "SUCCESS"
            }
            .expectNextMatches {
                it.purpose.name == "Utilities" && it.moneyAmount == 150 && !it.isDonation && it.status == "PENDING"
            }
            .verifyComplete()
    }

    @Test
    fun `addExpense - should add expense`() {
        val token = "validToken"
        val expenseManagerId = 321
        val request = TransactionDto(purposeId = 2, moneyAmount = 300)
        val response = TransactionResponse(
            dateTime = LocalDateTime.now(),
            purpose = PurposeResponse(2, "Maintenance"),
            userId = expenseManagerId,
            moneyAmount = 300,
            isDonation = false,
            status = "SUCCESS"
        )

        every { jwtUtils.extractUserId(token) } returns expenseManagerId
        every { transactionService.addTransaction(request, expenseManagerId, isDonation = false) } returns
                Mono.just(response)

        StepVerifier.create(controller.addExpense(request, token))
            .expectNextMatches {
                it.purpose.name == "Maintenance" && it.moneyAmount == 300 && !it.isDonation && it.status == "SUCCESS"
            }
            .verifyComplete()
    }
}
