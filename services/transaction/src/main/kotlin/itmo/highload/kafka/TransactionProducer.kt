package itmo.highload.kafka

import itmo.highload.api.dto.response.TransactionResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TransactionProducer(private val kafkaTemplate: KafkaTemplate<String, TransactionResponse>) {

    private val logger = LoggerFactory.getLogger(TransactionProducer::class.java)

    fun sendMessageToNewDonationTopic(transaction: TransactionResponse) {
        kafkaTemplate.send("new_donation", transaction)
        logger.info("Sent to Kafka new_donation: $transaction")
    }
}
