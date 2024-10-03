package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.dto.TransactionDto
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.User
import itmo.highload.model.enum.Role
import itmo.highload.repository.TransactionRepository
import itmo.highload.service.BalanceService
import itmo.highload.service.TransactionService
import itmo.highload.service.exception.NegativeBalanceException
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val balanceService: BalanceService = mockk()
    private val transactionService = TransactionService(transactionRepository, balanceService)

    private val user = User(
        id = 1,
        login = "customer",
        password = "123",
        role = Role.CUSTOMER,
        creationDate = LocalDate.now()
    )

    private val balance = Balance(
        id = 1,
        purpose = "food",
        moneyAmount = 200
    )

    private val transactionDto = TransactionDto(
        purposeId = 1,
        moneyAmount = 100
    )

    @Test
    fun `should add transaction and update balance successfully`() {

        val transaction = Transaction(
            id = 1,
            LocalDateTime.now(),
            user,
            balance,
            moneyAmount = 100,
            isDonation = true
        )

        every { balanceService.getById(transactionDto.purposeId!!) } returns balance
        every { transactionRepository.save(any()) } returns transaction
        every { balanceService.changeMoneyAmount(
            transactionDto.purposeId!!,
            true,
            transactionDto.moneyAmount!!)
        } returns balance

        val result = transactionService.addTransaction(transactionDto, user, isDonation = true)

        assertNotNull(result)
        verify { transactionRepository.save(any()) }
        verify { balanceService.changeMoneyAmount(transactionDto.purposeId!!, true, transactionDto.moneyAmount!!) }
    }

    @Test
    fun `should throw NegativeBalanceException when balance becomes negative`() {

        val transaction = Transaction(
            id = 1,
            LocalDateTime.now(),
            user,
            balance,
            moneyAmount = 100,
            isDonation = false
        )

        every { balanceService.getById(transactionDto.purposeId!!) } returns balance
        every { transactionRepository.save(any()) } returns transaction
        every { balanceService.changeMoneyAmount(
            transactionDto.purposeId!!,
            false,
            transactionDto.moneyAmount!!)
        } throws NegativeBalanceException("Insufficient funds to complete the transaction")

        val exception = assertThrows<NegativeBalanceException> {
            transactionService.addTransaction(transactionDto, user, isDonation = false)
        }

        assertEquals("Insufficient funds to complete the transaction", exception.message)
        verify { transactionRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when balance is not found`() {

        every { balanceService.getById(transactionDto.purposeId!!)
        } throws EntityNotFoundException("Failed to find Balance with id = ${transactionDto.purposeId}")

        val exception = assertThrows<EntityNotFoundException> {
            transactionService.addTransaction(transactionDto, user, isDonation = true)
        }

        assertEquals("Failed to find Balance with id = ${transactionDto.purposeId}", exception.message)
        verify(exactly = 0) { transactionRepository.save(any()) }
    }
}
