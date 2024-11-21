package itmo.highload.service

import io.mockk.*
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class BalanceServiceTest {

    private lateinit var balanceRepository: BalanceRepository
    private lateinit var balanceService: BalanceService

    @BeforeEach
    fun setup() {
        balanceRepository = mockk()
        balanceService = BalanceService(balanceRepository)
    }

    @Test
    fun `getById - should return Balance when found`() {
        val balanceId = 1
        val balance = Balance(id = balanceId, purpose = "Медицина", moneyAmount = 500)

        every { balanceRepository.findById(balanceId) } returns Mono.just(balance)

        StepVerifier.create(balanceService.getById(balanceId))
            .expectNextMatches { it.id == balanceId && it.purpose == "Медицина" && it.moneyAmount == 500 }
            .verifyComplete()

        verify { balanceRepository.findById(balanceId) }
    }

    @Test
    fun `getById - should return error when Balance not found`() {
        val balanceId = 2

        every { balanceRepository.findById(balanceId) } returns Mono.empty()

        StepVerifier.create(balanceService.getById(balanceId))
            .expectErrorMatches { it is EntityNotFoundException && it.message == "Failed " +
                    "to find Balance with id = $balanceId" }
            .verify()

        verify { balanceRepository.findById(balanceId) }
    }

    @Test
    fun `getAll - should return all Balances`() {
        val balances = listOf(
            Balance(id = 1, purpose = "Медицина", moneyAmount = 500),
            Balance(id = 2, purpose = "Питание", moneyAmount = 300)
        )

        every { balanceRepository.findAll() } returns Flux.fromIterable(balances)

        StepVerifier.create(balanceService.getAll())
            .expectNextMatches { it.id == 1 && it.purpose == "Медицина" && it.moneyAmount == 500 }
            .expectNextMatches { it.id == 2 && it.purpose == "Питание" && it.moneyAmount == 300 }
            .verifyComplete()

        verify { balanceRepository.findAll() }
    }

    @Test
    fun `addPurpose - should add new purpose when it does not exist`() {
        val purposeName = "Образование"
        val newBalance = Balance(id = 3, purpose = purposeName, moneyAmount = 0)

        every { balanceRepository.findByPurpose(purposeName) } returns Mono.empty()
        every { balanceRepository.save(any<Balance>()) } returns Mono.just(newBalance)

        StepVerifier.create(balanceService.addPurpose(purposeName))
            .expectNextMatches { it.id == 3 && it.purpose == purposeName && it.moneyAmount == 0 }
            .verifyComplete()

        verify { balanceRepository.findByPurpose(purposeName) }
        verify { balanceRepository.save(match { it.purpose == purposeName && it.moneyAmount == 0 }) }
    }

    @Test
    fun `addPurpose - should throw error when purpose already exists`() {
        val purposeName = "Медицина"
        val existingBalance = Balance(id = 1, purpose = purposeName, moneyAmount = 500)

        every { balanceRepository.findByPurpose(purposeName) } returns Mono.just(existingBalance)

        StepVerifier.create(balanceService.addPurpose(purposeName))
            .expectErrorMatches { it is EntityAlreadyExistsException && it.message == "Purpose with " +
                    "name '$purposeName' already exists" }
            .verify()

        verify { balanceRepository.findByPurpose(purposeName) }
        verify(exactly = 0) { balanceRepository.save(any<Balance>()) }
    }

    @Test
    fun `changeMoneyAmount - should update money amount for donation`() {
        val balanceId = 1
        val initialBalance = Balance(id = balanceId, purpose = "Медицина", moneyAmount = 500)
        val updatedBalance = initialBalance.copy(moneyAmount = 600)

        every { balanceRepository.findById(balanceId) } returns Mono.just(initialBalance)
        every { balanceRepository.save(any<Balance>()) } returns Mono.just(updatedBalance)

        StepVerifier.create(balanceService.changeMoneyAmount(balanceId, isDonation = true, moneyAmount = 100))
            .expectNextMatches { it.id == balanceId && it.moneyAmount == 600 }
            .verifyComplete()

        verify { balanceRepository.findById(balanceId) }
        verify { balanceRepository.save(match { it.moneyAmount == 600 }) }
    }

    @Test
    fun `changeMoneyAmount - should update money amount for expense`() {
        val balanceId = 1
        val initialBalance = Balance(id = balanceId, purpose = "Медицина", moneyAmount = 500)
        val updatedBalance = initialBalance.copy(moneyAmount = 400)

        every { balanceRepository.findById(balanceId) } returns Mono.just(initialBalance)
        every { balanceRepository.save(any<Balance>()) } returns Mono.just(updatedBalance)

        StepVerifier.create(balanceService.changeMoneyAmount(balanceId, isDonation = false, moneyAmount = 100))
            .expectNextMatches { it.id == balanceId && it.moneyAmount == 400 }
            .verifyComplete()

        verify { balanceRepository.findById(balanceId) }
        verify { balanceRepository.save(match { it.moneyAmount == 400 }) }
    }

    @Test
    fun `changeMoneyAmount - should throw error for insufficient funds`() {
        val balanceId = 1
        val initialBalance = Balance(id = balanceId, purpose = "Медицина", moneyAmount = 500)

        every { balanceRepository.findById(balanceId) } returns Mono.just(initialBalance)

        StepVerifier.create(balanceService.changeMoneyAmount(balanceId, isDonation = false, moneyAmount = 600))
            .expectErrorMatches { it is NegativeBalanceException && it.message == "Insufficient funds " +
                    "to complete the transaction" }
            .verify()

        verify { balanceRepository.findById(balanceId) }
        verify(exactly = 0) { balanceRepository.save(any<Balance>()) }
    }
}
