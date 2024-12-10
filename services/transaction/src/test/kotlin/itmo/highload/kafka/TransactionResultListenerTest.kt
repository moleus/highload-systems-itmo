package itmo.highload.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.interactor.TransactionService
import itmo.highload.infrastructure.kafka.TransactionResultListener
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class TransactionResultListenerTest {

    private lateinit var listener: TransactionResultListener
    private val transactionService: TransactionService = mockk()

    @BeforeEach
    fun setup() {
        listener = TransactionResultListener(transactionService)
    }

    @Test
    fun `listenToTransactionResultTopic - should confirm transaction when success`() {
        val message = TransactionResultMessage(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            transactionId = 1,
            balanceId = 42,
            moneyAmount = 3000,
            isDonation = true,
            success = true,
            message = "Transaction successful"
        )

        every { transactionService.confirmTransaction(any()) } returns Mono.empty()

        listener.listenToTransactionResultTopic(message)

        verify {
            transactionService.confirmTransaction(
                withArg { result ->
                    assert(result.transactionId == message.transactionId)
                    assert(result.success)
                }
            )
        }
    }

    @Test
    fun `listenToTransactionResultTopic - should rollback transaction when not success`() {
        val message = TransactionResultMessage(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            transactionId = 2,
            balanceId = 43,
            moneyAmount = 1000,
            isDonation = false,
            success = false,
            message = "Transaction failed"
        )

        every { transactionService.rollbackTransaction(any()) } returns Mono.empty()

        listener.listenToTransactionResultTopic(message)

        verify {
            transactionService.rollbackTransaction(
                withArg { transactionId ->
                    assert(transactionId == message.transactionId)
                }
            )
        }
    }
}
