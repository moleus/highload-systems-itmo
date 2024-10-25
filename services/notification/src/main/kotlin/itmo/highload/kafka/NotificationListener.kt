package itmo.highload.kafka

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.api.dto.response.TransactionResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class NotificationListener(private val messagingTemplate: SimpMessagingTemplate) {

    private val logger = LoggerFactory.getLogger(NotificationListener::class.java)

    @KafkaListener(topics = ["new_donation"])
    fun listenToNewDonationTopic(donation: TransactionResponse) {
        logger.info("Received message from Kafka: $donation")

        val message = "New donation for purpose \"${donation.purpose.name}\", amount: ${donation.moneyAmount}"
        messagingTemplate.convertAndSend("/topic/donations", message)
    }

    @KafkaListener(topics = ["adoption_request_created"])
    fun listenToAdoptionRequestCreatedTopic(adoptionRequest: AdoptionRequestResponse) {
        logger.info("Received message from Kafka: $adoptionRequest")

        val message =  "New adoption request"
        messagingTemplate.convertAndSend("/topic/adoption_requests", message)
    }

    @KafkaListener(topics = ["adoption_request_changed"])
    fun listenToAdoptionRequestChangedTopic(adoptionRequest: AdoptionRequestResponse) {
        logger.info("Received message from Kafka: $adoptionRequest")

        val message = "Adoption request ${adoptionRequest.status}"
        messagingTemplate.convertAndSend("/topic/adoption_requests/${adoptionRequest.customerId}", message)
    }
}
