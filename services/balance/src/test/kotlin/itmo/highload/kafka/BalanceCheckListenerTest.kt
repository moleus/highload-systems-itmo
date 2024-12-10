package itmo.highload.kafka

import io.mockk.*
import itmo.highload.domain.interactor.BalanceService
import itmo.highload.infrastructure.kafka.BalanceCheckListener
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class BalanceCheckListenerTest {

    private val balanceService: BalanceService = mockk()
    private val kafkaTemplate: KafkaTemplate<String, TransactionResultMessage> = mockk(relaxed = true)
    private val listener = BalanceCheckListener(kafkaTemplate, balanceService)

    private val testTransactionBalanceMessage = TransactionBalanceMessage(
        dateTime = LocalDateTime.now(),
        transactionId = 1,
        balanceId = 1,
        moneyAmount = 100,
        isDonation = true
    )

    @Test
    fun `listenToBalanceCheckTopic should handle successful balance adjustment`() {
        every {
            balanceService.checkAndAdjustBalance(
                testTransactionBalanceMessage.balanceId,
                testTransactionBalanceMessage.isDonation,
                testTransactionBalanceMessage.moneyAmount
            )
        } returns Mono.just(true)

        val slot = slot<TransactionResultMessage>()
        every { kafkaTemplate.send("transaction_result", capture(slot)) } returns mockk()

        listener.listenToBalanceCheckTopic(testTransactionBalanceMessage)

        verify { balanceService.checkAndAdjustBalance(any(), any(), any()) }
        verify { kafkaTemplate.send("transaction_result", any()) }

        val sentMessage = slot.captured
        assert(sentMessage.success)
        assert(sentMessage.transactionId == testTransactionBalanceMessage.transactionId)
    }

    @Test
    fun `listenToBalanceCheckTopic should handle insufficient balance`() {
        every {
            balanceService.checkAndAdjustBalance(
                testTransactionBalanceMessage.balanceId,
                testTransactionBalanceMessage.isDonation,
                testTransactionBalanceMessage.moneyAmount
            )
        } returns Mono.just(false)

        val slot = slot<TransactionResultMessage>()
        every { kafkaTemplate.send("transaction_result", capture(slot)) } returns mockk()

        listener.listenToBalanceCheckTopic(testTransactionBalanceMessage)

        verify { balanceService.checkAndAdjustBalance(any(), any(), any()) }
        verify { kafkaTemplate.send("transaction_result", any()) }

        val sentMessage = slot.captured
        assert(!sentMessage.success)
        assert(sentMessage.message == "Insufficient balance")
    }

    @Test
    fun `listenToBalanceCheckTopic should handle errors during processing`() {
        val exception = RuntimeException("Processing error")
        every {
            balanceService.checkAndAdjustBalance(
                testTransactionBalanceMessage.balanceId,
                testTransactionBalanceMessage.isDonation,
                testTransactionBalanceMessage.moneyAmount
            )
        } returns Mono.error(exception)

        val slot = slot<TransactionResultMessage>()
        every { kafkaTemplate.send("transaction_result", capture(slot)) } returns mockk()

        listener.listenToBalanceCheckTopic(testTransactionBalanceMessage)

        verify { balanceService.checkAndAdjustBalance(any(), any(), any()) }
        verify { kafkaTemplate.send("transaction_result", any()) }

        val sentMessage = slot.captured
        assert(!sentMessage.success)
        assert(sentMessage.message == exception.message)
    }

    @Test
    fun `listenToRollBackTopic should handle successful rollback`() {
        every {
            balanceService.rollbackBalance(
                testTransactionBalanceMessage.balanceId,
                testTransactionBalanceMessage.isDonation,
                testTransactionBalanceMessage.moneyAmount
            )
        } returns Mono.just(true)

        listener.listenToRollBackTopic(testTransactionBalanceMessage)

        verify { balanceService.rollbackBalance(any(), any(), any()) }
    }

    @Test
    fun `listenToRollBackTopic should handle errors during rollback`() {
        val exception = RuntimeException("Rollback error")
        every {
            balanceService.rollbackBalance(
                testTransactionBalanceMessage.balanceId,
                testTransactionBalanceMessage.isDonation,
                testTransactionBalanceMessage.moneyAmount
            )
        } returns Mono.error(exception)

        listener.listenToRollBackTopic(testTransactionBalanceMessage)

        verify { balanceService.rollbackBalance(any(), any(), any()) }
    }
}
