package itmo.highload.kafka

import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class TransactionResultListener(
    private val transactionRepository: TransactionRepository,
    private val kafkaTemplate: KafkaTemplate<String, TransactionResponse>
) {

    private val logger = LoggerFactory.getLogger(TransactionResultListener::class.java)

    @KafkaListener(topics = ["\${spring.kafka.consumer.transaction-result-topic}"], groupId = "transaction_result_group")
    fun listenToTransactionResultTopic(@Payload message: TransactionResultMessage) {
        val transactionId = message.transactionId
        if (message.success) {
            // Завершаем транзакцию
            transactionRepository.confirmTransaction(transactionId)
            logger.info("Transaction $transactionId successfully confirmed")
        } else {
            // Откатываем транзакцию
            transactionRepository.rollbackTransaction(transactionId)
            logger.warn("Transaction $transactionId rolled back due to insufficient balance")
        }
    }
}
