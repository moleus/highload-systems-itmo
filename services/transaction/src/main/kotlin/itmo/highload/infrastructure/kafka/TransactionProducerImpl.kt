package itmo.highload.infrastructure.kafka

import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.TransactionProducer
import itmo.highload.kafka.TransactionBalanceMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TransactionProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>
): TransactionProducer {

    private val logger = LoggerFactory.getLogger(TransactionProducer::class.java)

    @Value("\${spring.kafka.producer.new-donation-topic}")
    lateinit var newDonationTopic: String

    @Value("\${spring.kafka.producer.balance-change-topic}")
    lateinit var balanceCheckTopic: String

    @Value("\${spring.kafka.producer.roll-back-topic}")
    lateinit var rollBackTopic: String

    override fun sendMessageToNewDonationTopic(transaction: TransactionResponse) {
        kafkaTemplate.send(newDonationTopic, transaction)
        logger.info("Sent to Kafka $newDonationTopic: $transaction")
    }

    override fun sendMessageToBalanceCheck(transaction: TransactionBalanceMessage) {
        kafkaTemplate.send(balanceCheckTopic, transaction)
        logger.info("Sent to Kafka $balanceCheckTopic: $transaction")
    }

    override fun sendRollBackMessage(transaction: TransactionBalanceMessage) {
        kafkaTemplate.send(rollBackTopic, transaction)
        logger.info("Sent to Kafka $rollBackTopic: $transaction")
    }
}
