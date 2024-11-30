package itmo.highload.kafka


import io.mockk.mockk
import io.mockk.verify
import io.mockk.every
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.infrastructure.kafka.TransactionProducerImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class TransactionProducerImplTest {

    private val kafkaTemplate: KafkaTemplate<String, Any> = mockk(relaxed = true)
    private lateinit var producer: TransactionProducerImpl

    @BeforeEach
    fun setup() {
        producer = TransactionProducerImpl(kafkaTemplate).apply {
            newDonationTopic = "new-donation-topic"
            balanceCheckTopic = "balance-check-topic"
            rollBackTopic = "roll-back-topic"
        }
    }

    @Test
    fun `sendMessageToNewDonationTopic - should send message to correct topic`() {
        val transaction = TransactionResponse(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            purpose = mockk(),
            userId = 123,
            moneyAmount = 5000,
            isDonation = true,
            status = "COMPLETED",
            id = 1
        )

        producer.sendMessageToNewDonationTopic(transaction)

        verify { kafkaTemplate.send(producer.newDonationTopic, transaction) }
    }

    @Test
    fun `sendMessageToBalanceCheck - should send message to correct topic`() {
        val transaction = TransactionBalanceMessage(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            transactionId = 1,
            balanceId = 42,
            moneyAmount = 3000,
            isDonation = false
        )

        producer.sendMessageToBalanceCheck(transaction)

        verify { kafkaTemplate.send(producer.balanceCheckTopic, transaction) }
    }

    @Test
    fun `sendRollBackMessage - should send message to correct topic`() {
        val transaction = TransactionBalanceMessage(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            transactionId = 1,
            balanceId = 42,
            moneyAmount = 3000,
            isDonation = true
        )

        every { kafkaTemplate.send(producer.rollBackTopic, transaction) } returns mockk()

        producer.sendRollBackMessage(transaction)

        verify { kafkaTemplate.send(producer.rollBackTopic, transaction) }
    }

}
