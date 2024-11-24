package itmo.highload.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.TransactionProducer
import itmo.highload.domain.TransactionRepository
import itmo.highload.domain.interactor.BalanceService
import itmo.highload.domain.interactor.TransactionService
import itmo.highload.infrastructure.postgres.model.Transaction
import itmo.highload.kafka.TransactionResultMessage
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val balanceService: BalanceService = mockk()
    private val transactionProducer: TransactionProducer = mockk()
    private val transactionService = TransactionService(transactionRepository, balanceService, transactionProducer, 0)

    private val userId = -1

    private val testTransaction = Transaction(
        id = 1,
        dateTime = LocalDateTime.now(),
        userId = 1,
        balanceId = 1,
        moneyAmount = 100,
        isDonation = false,
        status = "PENDING"
    )

    private val testBalanceResponse = BalanceResponse(
        id = 1,
        purpose = PurposeResponse(id = 1, name = "Test Purpose"),
        moneyAmount = 100
    )

    private val testTransactionResponse = TransactionResponse(
        dateTime = LocalDateTime.now(),
        purpose = PurposeResponse(id = 1, name = "Test Purpose"),
        userId = 1,
        moneyAmount = 100,
        isDonation = false,
        status = "PENDING"
    )

    private val testTransactionDto = TransactionDto(
        purposeId = 1,
        moneyAmount = 100
    )

    private val testTransactionResultMessage = TransactionResultMessage(
        dateTime = LocalDateTime.now(),
        transactionId = 1,
        balanceId = 1,
        moneyAmount = 100,
        isDonation = false,
        success = true,
        message = "Transaction completed successfully"
    )

    @Test
    fun `getExpenses should return expenses by purposeId`() {
        val purposeId = 1
        val token = "test-token"
        val transaction = testTransaction.copy(balanceId = purposeId)
        val balanceResponse = testBalanceResponse.copy(id = purposeId)

        every { transactionRepository.findByIsDonationAndBalanceId(false, purposeId) } returns
                Flux.just(transaction)
        every { balanceService.getBalanceById(token, purposeId) } returns Mono.just(balanceResponse)

        val result = transactionService.getExpenses(purposeId, token)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id }
            .verifyComplete()

        verify { transactionRepository.findByIsDonationAndBalanceId(false, purposeId) }
        verify { balanceService.getBalanceById(token, purposeId) }
    }

    @Test
    fun `getExpenses should return all expenses when purposeId is null`() {
        val token = "test-token"
        val transaction = testTransaction.copy()
        val balanceResponse = testBalanceResponse.copy(id = transaction.balanceId)

        every { transactionRepository.findByIsDonation(false) } returns Flux.just(transaction)
        every { balanceService.getBalanceById(token, transaction.balanceId) } returns Mono.just(balanceResponse)

        val result = transactionService.getExpenses(null, token)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id }
            .verifyComplete()

        verify { transactionRepository.findByIsDonation(false) }
        verify { balanceService.getBalanceById(token, transaction.balanceId) }
    }

    @Test
    fun `getDonations should return donations by purposeId`() {
        val purposeId = 2
        val token = "test-token"
        val transaction = testTransaction.copy(isDonation = true, balanceId = purposeId)
        val balanceResponse = testBalanceResponse.copy(id = purposeId)

        every { transactionRepository.findByIsDonationAndBalanceId(true, purposeId) } returns
                Flux.just(transaction)
        every { balanceService.getBalanceById(token, purposeId) } returns Mono.just(balanceResponse)

        val result = transactionService.getDonations(purposeId, token)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id }
            .verifyComplete()

        verify { transactionRepository.findByIsDonationAndBalanceId(true, purposeId) }
        verify { balanceService.getBalanceById(token, purposeId) }
    }

    @Test
    fun `addTransaction should save transaction and send Kafka messages`() {
        val transactionDto = testTransactionDto.copy(purposeId = 1, moneyAmount = 100)
        val transactionEntity = testTransaction.copy(id = 1)
        val response = testTransactionResponse.copy()

        every { transactionRepository.save(any()) } returns Mono.just(transactionEntity)
        every { transactionProducer.sendMessageToBalanceCheck(any()) } returns Unit

        val result = transactionService.addTransaction(transactionDto, managerId = 1, isDonation = false)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == response.moneyAmount && it.purpose.id == response.purpose.id }
            .verifyComplete()

        verify { transactionRepository.save(any()) }
        verify { transactionProducer.sendMessageToBalanceCheck(any()) }
    }

    @Test
    fun `rollbackTransaction should update status to CANCELED`() {
        val transactionId = 1
        val canceledTransaction = testTransaction.copy(status = "CANCELED")
        every { transactionRepository.updateStatus(transactionId, "CANCELED") } returns
                Mono.just(canceledTransaction)

        val result = transactionService.rollbackTransaction(transactionId)

        StepVerifier.create(result)
            .verifyComplete()

        verify { transactionRepository.updateStatus(transactionId, "CANCELED") }
    }

    @Test
    fun `confirmTransaction should update status to COMPLETED`() {
        val transactionMessage = testTransactionResultMessage.copy(transactionId = 1)

        val completedTransaction = testTransaction.copy(status = "COMPLETED")

        every { transactionRepository.updateStatus(transactionMessage.transactionId, "COMPLETED") } returns
                Mono.just(completedTransaction)

        val result = transactionService.confirmTransaction(transactionMessage)

        StepVerifier.create(result)
            .verifyComplete()

        verify { transactionRepository.updateStatus(transactionMessage.transactionId, "COMPLETED") }
    }

    @Test
    fun `confirmTransaction should rollback on error`() {
        val transactionMessage = testTransactionResultMessage.copy(transactionId = 1)

        every { transactionRepository.updateStatus(transactionMessage.transactionId, "COMPLETED") } returns
                Mono.error(
                    RuntimeException("Database error")
                )

        every { transactionRepository.updateStatus(transactionMessage.transactionId, "CANCELED") } returns
                Mono.just(testTransaction)
        every { transactionProducer.sendRollBackMessage(any()) } returns Unit

        val result = transactionService.confirmTransaction(transactionMessage)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify { transactionRepository.updateStatus(transactionMessage.transactionId, "COMPLETED") }
        verify { transactionRepository.updateStatus(transactionMessage.transactionId, "CANCELED") }
        verify { transactionProducer.sendRollBackMessage(any()) }
    }

    @Test
    fun `getAllByUser should return transactions for user and isDonation flag`() {
        val token = "test-token"
        val isDonation = true
        val transaction = testTransaction.copy(isDonation = isDonation, userId = userId)
        val balanceResponse = testBalanceResponse.copy(id = transaction.balanceId)

        every { transactionRepository.findByIsDonationAndUserId(isDonation, userId) } returns Flux.just(transaction)
        every { balanceService.getBalanceById(token, transaction.balanceId) } returns Mono.just(balanceResponse)

        val result = transactionService.getAllByUser(isDonation, userId, token)

        StepVerifier.create(result)
            .expectNextMatches {
                it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id
                        && it.isDonation == isDonation
            }
            .verifyComplete()

        verify { transactionRepository.findByIsDonationAndUserId(isDonation, userId) }
        verify { balanceService.getBalanceById(token, transaction.balanceId) }
    }

    @Test
    fun `getDonations should return all donations when purposeId is null`() {
        val token = "test-token"
        val transaction = testTransaction.copy(isDonation = true)
        val balanceResponse = testBalanceResponse.copy(id = transaction.balanceId)

        every { transactionRepository.findByIsDonation(true) } returns Flux.just(transaction)
        every { balanceService.getBalanceById(token, transaction.balanceId) } returns Mono.just(balanceResponse)

        val result = transactionService.getDonations(null, token)

        StepVerifier.create(result)
            .expectNextMatches {
                it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id && it.isDonation
            }
            .verifyComplete()

        verify { transactionRepository.findByIsDonation(true) }
        verify { balanceService.getBalanceById(token, transaction.balanceId) }
    }

    @Test
    fun `addTransaction should send donation message to Kafka when isDonation is true`() {
        val transactionDto = testTransactionDto.copy(purposeId = 1, moneyAmount = 100)
        val transactionEntity = testTransaction.copy(id = 1, isDonation = true)
        val response = testTransactionResponse.copy(isDonation = true)

        every { transactionRepository.save(any()) } returns Mono.just(transactionEntity)
        every { transactionProducer.sendMessageToBalanceCheck(any()) } returns Unit
        every { transactionProducer.sendMessageToNewDonationTopic(any()) } returns Unit

        val result = transactionService.addTransaction(transactionDto, managerId = 1, isDonation = true)

        StepVerifier.create(result)
            .expectNextMatches {
                it.moneyAmount == response.moneyAmount && it.purpose.id == response.purpose.id && it.isDonation
            }
            .verifyComplete()

        verify { transactionRepository.save(any()) }
        verify { transactionProducer.sendMessageToBalanceCheck(any()) }
        verify { transactionProducer.sendMessageToNewDonationTopic(any()) }
    }
}
