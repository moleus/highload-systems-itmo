package itmo.highload.infrastructure.kafka

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import itmo.highload.infrastructure.kafka.message.AdoptionRequestMessage
import itmo.highload.infrastructure.kafka.message.TransactionMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class NotificationListener(
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(NotificationListener::class.java)

    @KafkaListener(
        topics = ["\${spring.kafka.consumer.new-donation-topic}"],
        groupId = "donation_group"
    )
    fun listenToNewDonationTopic(@Payload message: String) {
        try {
            val donation = parseMessage(message, TransactionMessage::class.java)
            val notification = "New donation for purpose \"${donation.purpose.name}\", amount: ${donation.moneyAmount}"
            sendNotification(notification, "/topic/donations")

        } catch (e: JsonProcessingException) {
            logger.error("Failed to parse Kafka message: $message", e)
        }
    }

    @KafkaListener(
        topics = ["\${spring.kafka.consumer.adoption-request-created-topic}"],
        groupId = "request_created_group"
    )
    fun listenToAdoptionRequestCreatedTopic(@Payload message: String) {
        try {
            parseMessage(message, AdoptionRequestMessage::class.java)
            val notification =  "New adoption request"
            sendNotification(notification, "/topic/adoption_requests")

        } catch (e: JsonProcessingException) {
            logger.error("Failed to parse Kafka message: $message", e)
        }
    }

    @KafkaListener(
        topics = ["\${spring.kafka.consumer.adoption-request-changed-topic}"],
        groupId = "request_changed_group"
    )
    fun listenToAdoptionRequestChangedTopic(@Payload message: String) {
        try {
            val adoptionRequest = parseMessage(message, AdoptionRequestMessage::class.java)
            val notification = "Hey! Your request for an adoption has changed. New status: ${adoptionRequest.status}"
            sendNotification(notification, "/topic/adoption_requests/${adoptionRequest.customerId}")

        } catch (e: JsonProcessingException) {
            logger.error("Failed to parse Kafka message: $message", e)
        }
    }

    fun <T> parseMessage(message: String, targetType: Class<T>): T {
        val parsedMessage: T = objectMapper.readValue(message, targetType)
        logger.info("Received message from Kafka: $parsedMessage")
        return parsedMessage
    }

    fun sendNotification(notification: String, topic: String) {
        messagingTemplate.convertAndSend(topic, notification)
        logger.info("Send notification to $topic : $notification")
    }
}
