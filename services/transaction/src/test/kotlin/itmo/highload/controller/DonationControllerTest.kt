package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.TransactionService
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.LocalDateTime

class DonationControllerTest {

    private val transactionService = mockk<TransactionService>()
    private val jwtUtils = mockk<JwtUtils>()
    private val controller = DonationController(transactionService, jwtUtils)

    @Test
    fun `getDonations - should return all donations`() {
        val donations = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 1, name = "Медицина"), userId = 1, moneyAmount = 100, isDonation = true
            ),
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 2, name = "Питание"), userId = 2, moneyAmount = 200, isDonation = false
            ),
        )

        every { transactionService.getDonations(null) } returns Flux.fromIterable(donations)

        StepVerifier.create(controller.getDonations(null))
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(
                    2024, 12, 1,
                    12, 0
                ) && it.purpose.id == 1 && it.moneyAmount == 100 && it.userId == 1 && it.isDonation
            }
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(
                    2024, 12, 1,
                    12, 0
                ) && it.purpose.id == 2 && it.moneyAmount == 200 && it.userId == 2 && !it.isDonation
            }
            .verifyComplete()
    }

    @Test
    fun `getDonationsByCustomerForManager - should return donations by customer`() {
        val customerId = 2
        val donations = listOf(
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 1, name = "Медицина"), userId = 1, moneyAmount = 100, isDonation = true
            ),
            TransactionResponse(
                dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
                PurposeResponse(id = 2, name = "Питание"), userId = 2, moneyAmount = 200, isDonation = true
            ),
        )

        every { transactionService.getAllByUser(isDonation = true, userId = customerId) } returns
                Flux.fromIterable(donations.filter { it.userId == customerId && it.isDonation })


        StepVerifier.create(controller.getDonationsByCustomerForManager(customerId))
            .expectNextMatches {
                it.dateTime == LocalDateTime.of(2024, 12, 1, 12, 0) &&
                        it.purpose.id == 2 && it.moneyAmount == 200 && it.userId == 2 && it.isDonation
            }
            .verifyComplete()
    }


    @Test
    fun `addDonation - should add a new donation`() {
        val donationDto = TransactionDto(moneyAmount = 500, purposeId = 1)
        val donationResponse = TransactionResponse(
            dateTime = LocalDateTime.of(2024, 12, 1, 12, 0),
            PurposeResponse(id = 1, name = "Медицина"), userId = 1, moneyAmount = 500, isDonation = true
        )
        val token = "validToken"
        val userId = 1

        every { jwtUtils.extractUserId(token) } returns userId
        every { transactionService.addTransaction(donationDto, userId, isDonation = true) } returns
                Mono.just(donationResponse)

        StepVerifier.create(controller.addDonation(donationDto, token))
            .expectNextMatches { it.dateTime == LocalDateTime.of(2024, 12, 1, 12, 0) &&
                    it.purpose.id == 1 &&
                    it.purpose.name == "Медицина" &&
                    it.userId == 1 &&
                    it.moneyAmount == 500 &&
                    it.isDonation }
            .verifyComplete()
    }


    @Test
    fun `addDonation - should return 400 if invalid data provided`() {
        val donationDto = TransactionDto(moneyAmount = -50, purposeId = 1)
        val token = "validToken"
        val userId = 1

        every { jwtUtils.extractUserId(token) } returns userId
        every { transactionService.addTransaction(donationDto, userId, isDonation = true) } returns
                Mono.error(IllegalArgumentException("Invalid donation amount"))

        StepVerifier.create(controller.addDonation(donationDto, token))
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }

}
