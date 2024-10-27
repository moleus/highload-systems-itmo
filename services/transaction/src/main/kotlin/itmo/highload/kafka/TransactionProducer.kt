package itmo.highload.kafka

import itmo.highload.api.dto.response.TransactionResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TransactionProducer(private val kafkaTemplate: KafkaTemplate<String, TransactionResponse>) {

    private val logger = LoggerFactory.getLogger(TransactionProducer::class.java)

    @Value("\${spring.kafka.producer.new-donation-topic}")
    lateinit var newDonationTopic: String

    fun sendMessageToNewDonationTopic(transaction: TransactionResponse) {
        kafkaTemplate.send(newDonationTopic, transaction)
        logger.info("Sent to Kafka $newDonationTopic: $transaction")
    }
}
