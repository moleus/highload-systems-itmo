package itmo.highload.service

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
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
import itmo.highload.domain.mapper.TransactionMapper
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
    private val hazelcastInstance: HazelcastInstance = mockk()
    private val transactionService = TransactionService(transactionRepository, balanceService, transactionProducer,
        hazelcastInstance, 0)

    private val userId = -1
    private val purposeId = 1
    private val token = "test-token"

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
        status = "PENDING",
        id = 1
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
        every { transactionRepository.findAll() } returns Flux.just(transaction)
        every { balanceService.getBalanceById(token, purposeId) } returns Mono.just(balanceResponse)

        val transactionCacheMock = mockk<IMap<Int, TransactionResponse>>()
        every { hazelcastInstance.getMap<Int, TransactionResponse>("transaction") } returns transactionCacheMock
        every { transactionCacheMock.values } returns mutableListOf()
        every { transactionCacheMock[transaction.id] = any() } returns Unit

        val balanceCacheMock = mockk<IMap<Int, BalanceResponse>>()
        every { hazelcastInstance.getMap<Int, BalanceResponse>("balance") } returns balanceCacheMock
        every { balanceCacheMock.values } returns mutableListOf()

        val result = transactionService.getExpenses(purposeId, token)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id }
            .verifyComplete()

        verify { transactionRepository.findByIsDonationAndBalanceId(false, purposeId) }
        verify { balanceService.getBalanceById(token, purposeId) }
        verify { transactionCacheMock.values }
        verify { transactionCacheMock[transaction.id] = any() }
    }



    @Test
    fun `getExpenses should return all expenses when purposeId is null`() {
        val token = "test-token"
        val transaction = testTransaction.copy()
        val balanceResponse = testBalanceResponse.copy(id = transaction.balanceId)

        every { transactionRepository.findByIsDonation(false) } returns Flux.just(transaction)
        every { balanceService.getBalanceById(token, transaction.balanceId) } returns Mono.just(balanceResponse)
        val transactionCacheMock = mockk<IMap<Int, TransactionResponse>>()
        every { hazelcastInstance.getMap<Int, TransactionResponse>("transaction") } returns transactionCacheMock
        every { transactionCacheMock.values } returns mutableListOf()
        every { transactionCacheMock[transaction.id] = any() } returns Unit

        every { hazelcastInstance.getMap<Int, TransactionService>("balance")[1] } returns null
        every { transactionRepository.findAll() } returns Flux.just(transaction)


        val result = transactionService.getExpenses(null, token)

        StepVerifier.create(result)
            .expectNextMatches { it.moneyAmount == transaction.moneyAmount && it.purpose.id == balanceResponse.id }
            .verifyComplete()

        verify { transactionRepository.findByIsDonation(false) }
        verify { balanceService.getBalanceById(token, transaction.balanceId) }
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

        every { hazelcastInstance.getMap<Int, TransactionService>("transaction")[1] } returns null
        every { hazelcastInstance.getMap<Int, TransactionService>("balance")[1] } returns null

        val result = transactionService.confirmTransaction(transactionMessage)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify { transactionRepository.updateStatus(transactionMessage.transactionId, "COMPLETED") }
        verify { transactionRepository.updateStatus(transactionMessage.transactionId, "CANCELED") }
        verify { transactionProducer.sendRollBackMessage(any()) }
    }


}
