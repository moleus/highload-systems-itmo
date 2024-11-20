package itmo.highload.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import itmo.highload.kafka.message.AdoptionRequestMessage
import itmo.highload.kafka.message.TransactionMessage
import itmo.highload.kafka.message.AdoptionStatus
import itmo.highload.kafka.message.PurposeMessage
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
            isDonation = true
        )

        val messageJson = """{"dateTime":"2024-11-19T12:00:00","purpose":{"id":1,"name":"Animal care"},"userId":1,"moneyAmount":100,"isDonation":true}"""

        // Mock objectMapper to return the parsed TransactionMessage object
        every { objectMapper.readValue(messageJson, TransactionMessage::class.java) } returns transactionMessage

        // Call the listener method
        notificationListener.listenToNewDonationTopic(messageJson)

        // Verify that the notification was sent to the correct topic
        verify {
            messagingTemplate.convertAndSend("/topic/donations", "New donation for purpose \"Animal care\", amount: 100")
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

        val messageJson = """{"id":1,"dateTime":"2024-11-19T12:00:00","status":"PENDING","customerId":1,"managerId":null,"animalId":101}"""

        // Mock objectMapper to return the parsed AdoptionRequestMessage object
        every { objectMapper.readValue(messageJson, AdoptionRequestMessage::class.java) } returns adoptionRequestMessage

        // Call the listener method
        notificationListener.listenToAdoptionRequestCreatedTopic(messageJson)

        // Verify that the notification was sent to the correct topic
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

        val messageJson = """{"id":1,"dateTime":"2024-11-19T12:00:00","status":"APPROVED","customerId":1,"managerId":2,"animalId":101}"""

        // Mock objectMapper to return the parsed AdoptionRequestMessage object
        every { objectMapper.readValue(messageJson, AdoptionRequestMessage::class.java) } returns adoptionRequestMessage

        // Call the listener method
        notificationListener.listenToAdoptionRequestChangedTopic(messageJson)

        // Verify that the notification was sent to the correct customer topic
        verify {
            messagingTemplate.convertAndSend("/topic/adoption_requests/1", "Hey! Your request for an adoption has changed. New status: APPROVED")
        }
    }

//    @Test
//    fun `listenToNewDonationTopic - should log error when message parsing fails`() {
//        val messageJson = """{invalidJson"""
//
//        // Call the listener method with invalid message
//        notificationListener.listenToNewDonationTopic(messageJson)
//
//        // Verify that the error was logged
//        verify { messagingTemplate.convertAndSend(any(), any()) wasNot called }
//    }
}
