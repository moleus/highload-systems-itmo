package itmo.highload.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.BalanceRepository
import itmo.highload.domain.interactor.BalanceService
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.infrastructure.postgres.model.Balance
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class BalanceServiceTest {

    private val balanceRepository: BalanceRepository = mockk()
    private val delay: Long = 1
    private val balanceService = BalanceService(balanceRepository, delay)

    private val testBalance = Balance(id = 1, purpose = "test", moneyAmount = 100)

    @Test
    fun `should throw EntityNotFoundException when balance by id is not found`() {
        every { balanceRepository.findById(1) } returns Mono.empty()

        val result = balanceService.getBalanceById(1)

        StepVerifier.create(result)
            .expectErrorMatches { it is EntityNotFoundException && it.message == "Failed to find Balance with id = 1" }
            .verify()

        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should throw EntityAlreadyExistsException when purpose already exists`() {
        every { balanceRepository.findByPurpose("test") } returns Mono.just(testBalance)

        val result = balanceService.addPurpose("test")

        StepVerifier.create(result)
            .expectErrorMatches { it is EntityAlreadyExistsException && it.message == "Purpose with name " +
                    "'test' already exists" }
            .verify()

        verify { balanceRepository.findByPurpose("test") }
    }

    @Test
    fun `should adjust balance correctly for donation`() {
        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
        every { balanceRepository.save(any()) } returns Mono.just(testBalance.copy(moneyAmount = 150))

        val result = balanceService.checkAndAdjustBalance(1, isDonation = true, moneyAmount = 50)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
        verify { balanceRepository.save(testBalance.copy(moneyAmount = 150)) }
    }

    @Test
    fun `should fail to adjust balance for negative amount`() {
        every { balanceRepository.findById(1) } returns Mono.just(testBalance)

        val result = balanceService.checkAndAdjustBalance(1, isDonation = false, moneyAmount = 200)

        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should rollback balance correctly`() {
        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
        every { balanceRepository.save(any()) } returns Mono.just(testBalance.copy(moneyAmount = 50))

        val result = balanceService.rollbackBalance(1, isDonation = true, moneyAmount = 50)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
        verify { balanceRepository.save(testBalance.copy(moneyAmount = 50)) }
    }

}
