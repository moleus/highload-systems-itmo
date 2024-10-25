package itmo.highload.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TransactionProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    private val logger = LoggerFactory.getLogger(TransactionProducer::class.java)

    fun sendMessageToNewDonationTopic(message: String) {
        kafkaTemplate.send("new_donation", message)
        logger.info("Sent to Kafka new_donation: $message")
    }
}
