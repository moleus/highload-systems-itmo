package itmo.highload.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.TransactionDto
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.kafka.TransactionProducer
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val balanceService: BalanceService = mockk()
    private val transactionProducer: TransactionProducer = mockk()
    private val transactionService = TransactionService(transactionRepository, balanceService, transactionProducer)

    private val userId = -1

    private val balance = Balance(
        id = 1, purpose = "food", moneyAmount = 200
    )

    private val transactionDto = TransactionDto(
        purposeId = 1, moneyAmount = 100
    )

    private val transaction = Transaction(
        id = 1,
        dateTime = LocalDateTime.now(),
        userId = userId,
        balanceId = balance.id,
        moneyAmount = 100,
        isDonation = false
    )

    private val transactionResponse = TransactionMapper.toResponse(transaction, balance)


    @Test
    fun `should add transaction and update balance successfully`() {

        val transaction = Transaction(
            id = 1, LocalDateTime.now(), userId, balance.id, moneyAmount = 100, isDonation = true
        )

        every { balanceService.getById(transactionDto.purposeId!!) } returns Mono.just(balance)
        every { transactionRepository.save(any()) } returns Mono.just(transaction)
        every { transactionProducer.sendMessageToNewDonationTopic(any()) } returns Unit
        every {
            balanceService.changeMoneyAmount(
                transactionDto.purposeId!!, true, transactionDto.moneyAmount!!
            )
        } returns Mono.just(balance)


        val result = transactionService.addTransaction(transactionDto, userId, isDonation = true).block()

        assertNotNull(result)
        verify { transactionRepository.save(any()) }
        verify { balanceService.changeMoneyAmount(transactionDto.purposeId!!, true,
            transactionDto.moneyAmount!!) }
        verify { transactionProducer.sendMessageToNewDonationTopic(any()) }
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

    @Test
    fun `should get expenses for specific purpose`() {
        val purposeId = balance.id

        every { transactionRepository.findByIsDonationAndBalanceId(false, purposeId) } returns
                Flux.just(transaction)
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getExpenses(purposeId).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals(transactionResponse, result[0])
        verify { transactionRepository.findByIsDonationAndBalanceId(false, purposeId) }
        verify { balanceService.getById(balance.id) }
    }

    @Test
    fun `should get all expenses when no purpose specified`() {
        every { transactionRepository.findByIsDonation(false) } returns Flux.just(transaction)
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getExpenses(null).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals(transactionResponse, result[0])
        verify { transactionRepository.findByIsDonation(false) }
        verify { balanceService.getById(balance.id) }
    }

    @Test
    fun `should get donations for specific purpose`() {
        val purposeId = balance.id
        val donationTransaction = transaction.copy(isDonation = true)

        every { transactionRepository.findByIsDonationAndBalanceId(true, purposeId) } returns Flux.just(
            donationTransaction
        )
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getDonations(purposeId).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val expectedResponse = TransactionMapper.toResponse(donationTransaction, balance)
        assertEquals(expectedResponse, result[0])
        verify { transactionRepository.findByIsDonationAndBalanceId(true, purposeId) }
        verify { balanceService.getById(balance.id) }
    }

    @Test
    fun `should get all donations when no purpose specified`() {
        val donationTransaction = transaction.copy(isDonation = true)

        every { transactionRepository.findByIsDonation(true) } returns Flux.just(donationTransaction)
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getDonations(null).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val expectedResponse = TransactionMapper.toResponse(donationTransaction, balance)
        assertEquals(expectedResponse, result[0])
        verify { transactionRepository.findByIsDonation(true) }
        verify { balanceService.getById(balance.id) }
    }

    @Test
    fun `should get all transactions by user for donations`() {
        val donationTransaction = transaction.copy(isDonation = true)

        every { transactionRepository.findByIsDonationAndUserId(true, userId) } returns
                Flux.just(donationTransaction)
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getAllByUser(true, userId).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        val expectedResponse = TransactionMapper.toResponse(donationTransaction, balance)
        assertEquals(expectedResponse, result[0])
        verify { transactionRepository.findByIsDonationAndUserId(true, userId) }
        verify { balanceService.getById(balance.id) }
    }

    @Test
    fun `should get all transactions by user for expenses`() {
        every { transactionRepository.findByIsDonationAndUserId(false, userId) } returns
                Flux.just(transaction)
        every { balanceService.getById(balance.id) } returns Mono.just(balance)

        val result = transactionService.getAllByUser(false, userId).collectList().block()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals(transactionResponse, result[0])
        verify { transactionRepository.findByIsDonationAndUserId(false, userId) }
        verify { balanceService.getById(balance.id) }
    }
}
