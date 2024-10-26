package itmo.highload.kafka

import itmo.highload.kafka.message.AdoptionRequestMessage
import itmo.highload.kafka.message.TransactionMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class NotificationListener(private val messagingTemplate: SimpMessagingTemplate) {

    private val logger = LoggerFactory.getLogger(NotificationListener::class.java)

    @KafkaListener(topics = ["new_donation"], groupId = "donation_group")
    fun listenToNewDonationTopic(donation: TransactionMessage) {
        logger.info("Received message from Kafka: $donation")

        val message = "New donation for purpose \"${donation.purpose.name}\", amount: ${donation.moneyAmount}"
        messagingTemplate.convertAndSend("/topic/donations", message)
    }

    @KafkaListener(topics = ["adoption_request_created"], groupId = "request_created_group")
    fun listenToAdoptionRequestCreatedTopic(adoptionRequest: AdoptionRequestMessage) {
        logger.info("Received message from Kafka: $adoptionRequest")

        val message =  "New adoption request"
        messagingTemplate.convertAndSend("/topic/adoption_requests", message)
    }

    @KafkaListener(topics = ["adoption_request_changed"], groupId = "request_changed_group")
    fun listenToAdoptionRequestChangedTopic(adoptionRequest: AdoptionRequestMessage) {
        logger.info("Received message from Kafka: $adoptionRequest")

        val message = "Adoption request ${adoptionRequest.status}"
        messagingTemplate.convertAndSend("/topic/adoption_requests/${adoptionRequest.customerId}", message)
    }
}
