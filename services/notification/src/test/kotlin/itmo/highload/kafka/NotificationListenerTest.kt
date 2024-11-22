package itmo.highload.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.infrastructure.kafka.NotificationListener
import itmo.highload.infrastructure.kafka.message.AdoptionRequestMessage
import itmo.highload.infrastructure.kafka.message.AdoptionStatus
import itmo.highload.infrastructure.kafka.message.PurposeMessage
import itmo.highload.infrastructure.kafka.message.TransactionMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime

@EnableKafka
class NotificationListenerTest {

    private val messagingTemplate: SimpMessagingTemplate = mockk(relaxed = true)
    private val objectMapper: ObjectMapper = mockk(relaxed = true)
    private lateinit var notificationListener: NotificationListener

    @BeforeEach
    fun setUp() {
        notificationListener = NotificationListener(messagingTemplate, objectMapper)
    }

    @Test
    fun `listenToNewDonationTopic - should send notification to correct topic`() {
        val transactionMessage = TransactionMessage(
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            purpose = PurposeMessage(1, "Animal care"),
            userId = 1,
            moneyAmount = 100,
            isDonation = true,
            status = "SUCCESS"
        )

        val messageJson = """{"dateTime":"2024-11-19T12:00:00","purpose":{"id":1,"name":"Animal care"},"userId":
        |1,"moneyAmount":100,"isDonation":true, "status":"SUCCESS"}""".trimMargin()

        every { objectMapper.readValue(messageJson, TransactionMessage::class.java) } returns transactionMessage

        notificationListener.listenToNewDonationTopic(messageJson)

        verify {
            messagingTemplate.convertAndSend("/topic/donations", "New donation for purpose " +
                    "with id 1, amount: 100")
        }
    }


    @Test
    fun `listenToAdoptionRequestCreatedTopic - should send notification to correct topic`() {
        val adoptionRequestMessage = AdoptionRequestMessage(
            id = 1,
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            status = AdoptionStatus.PENDING,
            customerId = 1,
            managerId = null,
            animalId = 101
        )

        val messageJson = """{"id":1,"dateTime":"2024-11-19T12:00:00","status":"PENDING","customerId":1,
            |"managerId":null,"animalId":101}""".trimMargin()

        every { objectMapper.readValue(messageJson, AdoptionRequestMessage::class.java) } returns adoptionRequestMessage

        notificationListener.listenToAdoptionRequestCreatedTopic(messageJson)

        verify {
            messagingTemplate.convertAndSend("/topic/adoption_requests", "New adoption request")
        }
    }

    @Test
    fun `listenToAdoptionRequestChangedTopic - should send notification to customer topic`() {
        val adoptionRequestMessage = AdoptionRequestMessage(
            id = 1,
            dateTime = LocalDateTime.parse("2024-11-19T12:00:00"),
            status = AdoptionStatus.APPROVED,
            customerId = 1,
            managerId = 2,
            animalId = 101
        )

        val messageJson = """{"id":1,"dateTime":"2024-11-19T12:00:00","status":"APPROVED","customerId":1,
            |"managerId":2,"animalId":101}""".trimMargin()

        every { objectMapper.readValue(messageJson, AdoptionRequestMessage::class.java) } returns adoptionRequestMessage

        notificationListener.listenToAdoptionRequestChangedTopic(messageJson)

        verify {
            messagingTemplate.convertAndSend("/topic/adoption_requests/1", "Hey! " +
                    "Your request for an adoption has changed. New status: APPROVED")
        }
    }

}
