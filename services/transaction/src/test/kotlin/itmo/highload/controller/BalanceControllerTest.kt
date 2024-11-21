package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.model.Balance
import itmo.highload.service.BalanceService
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class BalanceControllerTest {

    private val balanceService = mockk<BalanceService>()
    private val controller = BalanceController(balanceService)

    @Test
    fun `getAllBalances - should return all balances`() {
        val balances = listOf(
            Balance(id = 1, purpose = "Медикаменты", moneyAmount = 100),
            Balance(id = 2, purpose = "Питание", moneyAmount = 200),
        )

        every { balanceService.getAll() } returns Flux.fromIterable(balances)

        StepVerifier.create(controller.getAllBalances())
            .expectNextMatches { it.purpose.id == 1 && it.purpose.name == "Медикаменты" && it.moneyAmount == 100 }
            .expectNextMatches { it.purpose.id == 2 && it.purpose.name == "Питание" && it.moneyAmount == 200 }
            .verifyComplete()
    }

    @Test
    fun `getBalanceById - should return balance by ID`() {
        val balanceId = 1
        val balance = Balance(id = 1, purpose = "Медикаменты", moneyAmount = 100)

        every { balanceService.getById(balanceId) } returns Mono.just(balance)

        StepVerifier.create(controller.getBalanceById(balanceId))
            .expectNextMatches { it.purpose.id == 1 && it.purpose.name == "Медикаменты" && it.moneyAmount == 100 }
            .verifyComplete()
    }

    @Test
    fun `getAllPurposes - should return all purposes`() {
        val balances = listOf(
            Balance(id = 1, purpose = "Purpose 1", moneyAmount = 100),
            Balance(id = 2, purpose = "Purpose 2", moneyAmount = 200),
        )

        every { balanceService.getAllPurposes() } returns Flux.fromIterable(balances)

        StepVerifier.create(controller.getAllPurposes())
            .expectNextMatches { it.id == 1 && it.name == "Purpose 1" }
            .expectNextMatches { it.id == 2 && it.name == "Purpose 2" }
            .verifyComplete()
    }

    @Test
    fun `addPurpose - should add a new purpose`() {
        val request = PurposeRequestDto(name = "New Purpose")
        val purpose = Balance(id = 3, purpose = "New Purpose", moneyAmount = 1000)

        every { balanceService.addPurpose(request.name) } returns Mono.just(purpose)

        StepVerifier.create(controller.addPurpose(request))
            .expectNextMatches { it.id == 3 && it.name == "New Purpose" }
            .verifyComplete()
    }

}
