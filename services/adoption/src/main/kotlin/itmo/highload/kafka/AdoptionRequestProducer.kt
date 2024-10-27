package itmo.highload.kafka

import itmo.highload.api.dto.response.AdoptionRequestResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class AdoptionRequestProducer(private val kafkaTemplate: KafkaTemplate<String, AdoptionRequestResponse>) {

    private val logger = LoggerFactory.getLogger(AdoptionRequestProducer::class.java)

    @Value("\${spring.kafka.producer.adoption-request-created-topic}")
    lateinit var adoptionRequestCreatedTopic: String

    @Value("\${spring.kafka.producer.adoption-request-changed-topic}")
    lateinit var adoptionRequestChangedTopic: String

    fun sendMessageToAdoptionRequestCreatedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send(adoptionRequestCreatedTopic, adoptionRequest)
        logger.info("Sent to Kafka $adoptionRequestCreatedTopic: $adoptionRequest")
    }

    fun sendMessageToAdoptionRequestChangedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send(adoptionRequestChangedTopic, adoptionRequest)
        logger.info("Sent to Kafka $adoptionRequestChangedTopic: $adoptionRequest")
    }
}
