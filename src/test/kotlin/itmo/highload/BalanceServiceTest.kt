package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.BalanceService
import itmo.highload.service.exception.EntityAlreadyExistsException
import itmo.highload.service.exception.NegativeBalanceException
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class BalanceServiceTest {

    private val balanceRepository: BalanceRepository = mockk()
    private val balanceService = BalanceService(balanceRepository)

    @Test
    fun `should return balance if found by id`() {
        val balance = Balance(id = 1, purpose = "TestBalance", moneyAmount = 100)
        every { balanceRepository.findById(1) } returns Optional.of(balance)

        val result = balanceService.getById(1)

        assertEquals(balance, result)
        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should throw EntityNotFoundException if balance not found by id`() {
        every { balanceRepository.findById(1) } returns Optional.empty()

        val exception = assertThrows<EntityNotFoundException> {
            balanceService.getById(1)
        }

        assertEquals("Failed to find Balance with id = 1", exception.message)
        verify { balanceRepository.findById(1) }
    }

    @Test
    fun `should add new purpose if it does not exist`() {
        val newBalance = Balance(purpose = "purpose", moneyAmount = 0)

        every { balanceRepository.save(any()) } returns newBalance
        every { balanceRepository.findByPurpose("purpose") } returns null

        val result = balanceService.addPurpose("purpose")

        assertEquals("purpose", result.purpose)
        verify { balanceRepository.save(any()) }
    }

    @Test
    fun `should throw EntityAlreadyExistsException if purpose already exists`() {

        val balance = Balance(purpose = "existing purpose", moneyAmount = 50)
        every { balanceRepository.findByPurpose("existing purpose") } returns balance

        val exception = assertThrows<EntityAlreadyExistsException> {
            balanceService.addPurpose("existing purpose")
        }

        assertEquals("Purpose with name 'existing purpose' already exists", exception.message)
        verify(exactly = 0) { balanceRepository.save(any()) }
    }

    @Test
    fun `should add money when donation`() {
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 100)

        every { balanceRepository.findById(1) } returns Optional.of(balance)
        every { balanceRepository.save(any()) } returns balance.copy(moneyAmount = 200)

        val updatedBalance = balanceService.changeMoneyAmount(1, isDonation = true, moneyAmount = 100)

        assertEquals(200, updatedBalance.moneyAmount)
        verify { balanceRepository.save(balance) }
    }

    @Test
    fun `should subtract money when not donation`() {
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 100)

        every { balanceRepository.findById(1) } returns Optional.of(balance)
        every { balanceRepository.save(any()) } returns balance.copy(moneyAmount = 50)

        val updatedBalance = balanceService.changeMoneyAmount(1, isDonation = false, moneyAmount = 50)

        assertEquals(50, updatedBalance.moneyAmount)
        verify { balanceRepository.save(balance) }
    }

    @Test
    fun `should throw NegativeBalanceException when balance goes negative`() {
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 50)

        every { balanceRepository.findById(1) } returns Optional.of(balance)

        val exception = assertThrows<NegativeBalanceException> {
            balanceService.changeMoneyAmount(1, isDonation = false, moneyAmount = 100)
        }

        assertEquals("Insufficient funds to complete the transaction", exception.message)

        verify(exactly = 0) { balanceRepository.save(any()) }
    }
}
