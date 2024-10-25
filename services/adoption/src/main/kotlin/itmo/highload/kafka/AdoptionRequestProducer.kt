package itmo.highload.kafka

import itmo.highload.api.dto.response.AdoptionRequestResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class AdoptionRequestProducer(private val kafkaTemplate: KafkaTemplate<String, AdoptionRequestResponse>) {

    private val logger = LoggerFactory.getLogger(AdoptionRequestProducer::class.java)

    fun sendMessageToCreatedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send("adoption_request_created", adoptionRequest)
        logger.info("Sent to Kafka adoption_request_created: $adoptionRequest")
    }

    fun sendMessageToChangedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send("adoption_request_changed", adoptionRequest)
        logger.info("Sent to Kafka adoption_request_changed: $adoptionRequest")
    }
}
