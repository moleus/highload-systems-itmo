package itmo.highload.infrastructure.kafka

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.domain.AdoptionRequestProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class AdoptionRequestProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, AdoptionRequestResponse>
): AdoptionRequestProducer {

    private val logger = LoggerFactory.getLogger(AdoptionRequestProducerImpl::class.java)

    @Value("\${spring.kafka.producer.adoption-request-created-topic}")
    lateinit var adoptionRequestCreatedTopic: String

    @Value("\${spring.kafka.producer.adoption-request-changed-topic}")
    lateinit var adoptionRequestChangedTopic: String

    override fun sendMessageToAdoptionRequestCreatedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send(adoptionRequestCreatedTopic, adoptionRequest)
        logger.info("Sent to Kafka $adoptionRequestCreatedTopic: $adoptionRequest")
    }

    override fun sendMessageToAdoptionRequestChangedTopic(adoptionRequest: AdoptionRequestResponse) {
        kafkaTemplate.send(adoptionRequestChangedTopic, adoptionRequest)
        logger.info("Sent to Kafka $adoptionRequestChangedTopic: $adoptionRequest")
    }
}
