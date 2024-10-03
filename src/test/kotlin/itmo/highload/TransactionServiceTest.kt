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

    @Test
    fun `should add transaction and update balance successfully`() {
        val user = User(
            id = 1,
            login = "customer",
            password = "123",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 200)
        val transaction = Transaction(
            id = 1,
            LocalDateTime.now(),
            user,
            balance,
            moneyAmount = 1,
            isDonation = true)
        val donationDto = TransactionDto(purposeId = 1, moneyAmount = 100)

        every { balanceService.getById(donationDto.purposeId!!) } returns balance
        every { transactionRepository.save(any()) } returns transaction
        every { balanceService.changeMoneyAmount(
            donationDto.purposeId!!,
            true,
            donationDto.moneyAmount!!)
        } returns balance

        val result = transactionService.addTransaction(donationDto, user, isDonation = true)

        assertNotNull(result)
        verify { transactionRepository.save(any()) }
        verify { balanceService.changeMoneyAmount(donationDto.purposeId!!, true, donationDto.moneyAmount!!) }
    }

    @Test
    fun `should throw NegativeBalanceException when balance becomes negative`() {
        val user = User(
            id = 1,
            login = "customer",
            password = "123",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )
        val balance = Balance(id = 1, purpose = "food", moneyAmount = 200)
        val transaction = Transaction(
            id = 1,
            LocalDateTime.now(),
            user,
            balance,
            moneyAmount = 1,
            isDonation = true)
        val donationDto = TransactionDto(purposeId = 1, moneyAmount = 300)

        every { balanceService.getById(donationDto.purposeId!!) } returns balance
        every { transactionRepository.save(any()) } returns transaction
        every { balanceService.changeMoneyAmount(
            donationDto.purposeId!!,
            false,
            donationDto.moneyAmount!!)
        } throws NegativeBalanceException("Insufficient funds to complete the transaction")

        val exception = assertThrows<NegativeBalanceException> {
            transactionService.addTransaction(donationDto, user, isDonation = false)
        }

        assertEquals("Insufficient funds to complete the transaction", exception.message)
        verify { transactionRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when balance is not found`() {
        val donationDto = TransactionDto(purposeId = 1, moneyAmount = 100)
        val user = User(
            id = 1,
            login = "customer",
            password = "123",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )

        every { balanceService.getById(donationDto.purposeId!!)
        } throws EntityNotFoundException("Failed to find Balance with id = ${donationDto.purposeId}")

        val exception = assertThrows<EntityNotFoundException> {
            transactionService.addTransaction(donationDto, user, isDonation = true)
        }

        assertEquals("Failed to find Balance with id = ${donationDto.purposeId}", exception.message)
        verify(exactly = 0) { transactionRepository.save(any()) }
    }
}
