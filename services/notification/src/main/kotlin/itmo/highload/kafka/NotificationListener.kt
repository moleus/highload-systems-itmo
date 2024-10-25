package itmo.highload.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationListener {

    private val logger = LoggerFactory.getLogger(NotificationListener::class.java)

    @KafkaListener(topics = ["new_donation"])
    fun listen(record: ConsumerRecord<String, String>) {
        val message = record.value()
        val key = record.key()

        logger.info("Received message from Kafka - Key: $key, Value: $message")
    }

    @KafkaListener(topics = ["adoption_request_created"])
    fun listen(record: ConsumerRecord<String, String>) {
        val message = record.value()
        val key = record.key()

        logger.info("Received message from Kafka - Key: $key, Value: $message")
    }

    @KafkaListener(topics = ["adoption_request_changed"])
    fun listen(record: ConsumerRecord<String, String>) {
        val message = record.value()
        val key = record.key()

        logger.info("Received message from Kafka - Key: $key, Value: $message")
    }
}
