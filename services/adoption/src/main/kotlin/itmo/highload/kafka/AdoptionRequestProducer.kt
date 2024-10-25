package itmo.highload.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class AdoptionRequestProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    private val logger = LoggerFactory.getLogger(AdoptionRequestProducer::class.java)

    fun sendMessageToCreatedTopic(message: String) {
        kafkaTemplate.send("adoption_request_created", message)
        logger.info("Sent to Kafka adoption_request_created: $message")
    }

    fun sendMessageToChangedTopic(message: String) {
        kafkaTemplate.send("adoption_request_changed", message)
        logger.info("Sent to Kafka adoption_request_changed: $message")
    }
}
