package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.TransactionDto
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.repository.TransactionRepository
import itmo.highload.service.BalanceService
import itmo.highload.service.TransactionService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val balanceService: BalanceService = mockk()
    private val transactionService = TransactionService(transactionRepository, balanceService)

    private val userId = -1

    private val balance = Balance(
        id = 1, purpose = "food", moneyAmount = 200
    )

    private val transactionDto = TransactionDto(
        purposeId = 1, moneyAmount = 100
    )

    @Test
    fun `should add transaction and update balance successfully`() {

        val transaction = Transaction(
            id = 1, LocalDateTime.now(), userId, balance.id, moneyAmount = 100, isDonation = true
        )

        every { balanceService.getById(transactionDto.purposeId!!) } returns Mono.just(balance)
        every { transactionRepository.save(any()) } returns Mono.just(transaction)
        every {
            balanceService.changeMoneyAmount(
                transactionDto.purposeId!!, true, transactionDto.moneyAmount!!
            )
        } returns Mono.just(balance)

        val result = transactionService.addTransaction(transactionDto, userId, isDonation = true).block()

        assertNotNull(result)
        verify { transactionRepository.save(any()) }
        verify { balanceService.changeMoneyAmount(transactionDto.purposeId!!, true, transactionDto.moneyAmount!!) }
    }

    @Test
    fun `should throw NegativeBalanceException when balance becomes negative`() {
        val transaction = Transaction(
            id = 1, LocalDateTime.now(), userId, balance.id, moneyAmount = 100, isDonation = false
        )

        every { balanceService.getById(transactionDto.purposeId!!) } returns Mono.just(balance)
        every { transactionRepository.save(any()) } returns Mono.just(transaction)
        every {
            balanceService.changeMoneyAmount(
                transactionDto.purposeId!!, false, transactionDto.moneyAmount!!
            )
        } returns Mono.error(NegativeBalanceException("Insufficient funds to complete the transaction"))

        val exception = assertThrows<NegativeBalanceException> {
            transactionService.addTransaction(transactionDto, userId, isDonation = false).block()
        }

        assertEquals("Insufficient funds to complete the transaction", exception.message)
        verify { transactionRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when balance is not found`() {

        every { balanceService.getById(transactionDto.purposeId!!) } returns
                Mono.error(EntityNotFoundException("Failed to find Balance with id = ${transactionDto.purposeId}"))

        val exception = assertThrows<EntityNotFoundException> {
            transactionService.addTransaction(transactionDto, userId, isDonation = true).block()
        }

        assertEquals("Failed to find Balance with id = ${transactionDto.purposeId}", exception.message)
        verify(exactly = 0) { transactionRepository.save(any()) }
    }
}
