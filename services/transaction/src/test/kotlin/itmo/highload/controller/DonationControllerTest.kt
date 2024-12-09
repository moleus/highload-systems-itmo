package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.interactor.TransactionService
import itmo.highload.infrastructure.http.DonationController
import itmo.highload.security.jwt.JwtUtils
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class DonationControllerTest {

    private val transactionService = mockk<TransactionService>()
    private val jwtUtils = mockk<JwtUtils>()
    private val controller = DonationController(transactionService, jwtUtils)

    @Test
    fun `getDonations - should return donations`() {
        val purposeId = 1
        val token = "validToken"
        val donations = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(1, "Education"),
                userId = 123,
                moneyAmount = 500,
                isDonation = true,
                status = "SUCCESS",
                id = 1
            ),
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(2, "Health"),
                userId = 124,
                moneyAmount = 700,
                isDonation = true,
                status = "PENDING",
                id = 1
            )
        )

        every { transactionService.getDonations(purposeId, token) } returns Flux.fromIterable(donations)

        StepVerifier.create(controller.getDonations(purposeId, token))
            .expectNextMatches {
                it.purpose.name == "Education" && it.moneyAmount == 500 && it.isDonation && it.status == "SUCCESS"
            }
            .expectNextMatches {
                it.purpose.name == "Health" && it.moneyAmount == 700 && it.isDonation && it.status == "PENDING"
            }
            .verifyComplete()
    }

    @Test
    fun `addDonation - should add donation`() {
        val token = "validToken"
        val userId = 123
        val request = TransactionDto(purposeId = 1, moneyAmount = 500)
        val response = TransactionResponse(
            dateTime = LocalDateTime.now(),
            purpose = PurposeResponse(1, "Education"),
            userId = userId,
            moneyAmount = 500,
            isDonation = true,
            status = "SUCCESS",
            id = 1
        )

        every { jwtUtils.extractUserId(token) } returns userId
        every { transactionService.addTransaction(request, userId, isDonation = true) } returns Mono.just(response)

        StepVerifier.create(controller.addDonation(request, token))
            .expectNextMatches {
                it.purpose.name == "Education" && it.moneyAmount == 500 && it.isDonation && it.status == "SUCCESS"
            }
            .verifyComplete()
    }
    @Test
    fun `getDonationsByCustomerForManager - should return donations for a specific customer`() {
        val customerId = 123
        val token = "validToken"
        val donations = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(1, "Education"),
                userId = customerId,
                moneyAmount = 500,
                isDonation = true,
                status = "SUCCESS",
                id = 1
            ),
            TransactionResponse(
                dateTime = LocalDateTime.now(),
                purpose = PurposeResponse(2, "Health"),
                userId = customerId,
                moneyAmount = 700,
                isDonation = true,
                status = "PENDING",
                id = 1
            )
        )

        every { transactionService.getAllByUser(isDonation = true, userId = customerId, token) } returns
                Flux.fromIterable(donations)

        StepVerifier.create(controller.getDonationsByCustomerForManager(customerId, token))
            .expectNextMatches {
                it.userId == customerId && it.purpose.name == "Education" && it.moneyAmount == 500 && it.isDonation
                        && it.status == "SUCCESS"
            }
            .expectNextMatches {
                it.userId == customerId && it.purpose.name == "Health" && it.moneyAmount == 700 && it.isDonation
                        && it.status == "PENDING"
            }
            .verifyComplete()
    }
}
