package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.BalanceService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError

class BalanceServiceTest {

    private val balanceRepository: BalanceRepository = mockk()
    private val balanceService = BalanceService(balanceRepository)

    @Test
    fun `should return balance if found by id`() {
        val balance = Balance(id = 1, purpose = "TestBalance", moneyAmount = 100)
        every { balanceRepository.findById(1) } returns Mono.just(balance)

        val result = balanceService.getById(1)

        result.test().expectNext(balance).verifyComplete()
        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should throw EntityNotFoundException if balance not found by id`() {
        every { balanceRepository.findById(1) } returns Mono.empty()

        val exception = assertThrows<NegativeBalanceException> {
            balanceService.getById(1).test().verifyError(EntityNotFoundException::class)
        }

        assertEquals("Failed to find Balance with id = 1", exception.message)
        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should add new purpose if it does not exist`() {
        val newBalance = Balance(purpose = "purpose", moneyAmount = 0)

        every { balanceRepository.save(any()) } returns Mono.just(newBalance)
        every { balanceRepository.findByPurpose("purpose") } returns Mono.empty()

        balanceService.addPurpose("purpose").test().expectNextMatches {
                it.purpose == "purpose" && it.moneyAmount == 0
            }.verifyComplete()

        verify { balanceRepository.save(any()) }
    }

    @Test
    fun `should throw EntityAlreadyExistsException if purpose already exists`() {

        val balance = Balance(purpose = "existing purpose", moneyAmount = 50)
        every { balanceRepository.findByPurpose("existing purpose") } returns Mono.just(balance)

        val exception = assertThrows<EntityAlreadyExistsException> {
            balanceService.addPurpose("existing purpose").test().verifyError(EntityAlreadyExistsException::class)
        }

        assertEquals("Purpose with name 'existing purpose' already exists", exception.message)
        verify(exactly = 0) { balanceRepository.save(any()) }
    }

    @Test
    fun `should add money when donation`() {
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 100)

        every { balanceRepository.findById(1) } returns Mono.just(balance)
        every { balanceRepository.save(any()) } returns Mono.just(balance.copy(moneyAmount = 200))
        balanceService.changeMoneyAmount(1, isDonation = true, moneyAmount = 100).test()
            .expectNextMatches { it.moneyAmount == 200 }.verifyComplete()

        verify { balanceRepository.save(balance) }
    }

    @Test
    fun `should subtract money when not donation`() {
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 100)

        every { balanceRepository.save(any()) } returns Mono.just(balance.copy(moneyAmount = 50))
        balanceService.changeMoneyAmount(1, isDonation = false, moneyAmount = 50).test()
            .expectNextMatches { it.moneyAmount == 50 }.verifyComplete()

        verify { balanceRepository.save(balance) }
    }

    @Test
    fun `should throw NegativeBalanceException when balance goes negative`() {
        val exception = assertThrows<NegativeBalanceException> {
            balanceService.changeMoneyAmount(1, isDonation = false, moneyAmount = 100).test()
                .verifyError(NegativeBalanceException::class)
        }

        assertEquals("Insufficient funds to complete the transaction", exception.message)

        verify(exactly = 0) { balanceRepository.save(any()) }
    }
}
