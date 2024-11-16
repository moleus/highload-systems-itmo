package itmo.highload.kafka

import itmo.highload.service.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class TransactionResultListener(
    private val transactionService: TransactionService,
) {

    private val logger = LoggerFactory.getLogger(TransactionResultListener::class.java)

    @KafkaListener(
        topics = ["\${spring.kafka.consumer.transaction-result-topic}"],
        groupId = "transaction_result_group"
    )
    fun listenToTransactionResultTopic(@Payload message: TransactionResultMessage) {
        val transactionId = message.transactionId
        if (message.success) {
            val transactionResultMessage = TransactionResultMessage(
                dateTime = message.dateTime,
                transactionId = transactionId,
                balanceId = message.balanceId,
                moneyAmount = message.moneyAmount,
                isDonation = message.isDonation,
                success = true,
                message = "Transaction successful"
            )
            transactionService.confirmTransaction(transactionResultMessage)
                .doOnSuccess {
                    logger.info("Transaction $transactionId successfully confirmed")
                }
                .doOnError { error ->
                    logger.error("Failed to confirm transaction $transactionId: ${error.message}")
                }
                .subscribe()
        } else {
            transactionService.rollbackTransaction(transactionId)
                .doOnSuccess {
                    logger.warn("Transaction $transactionId rolled back due to insufficient balance")
                }
                .doOnError { error ->
                    logger.error("Failed to rollback transaction $transactionId: ${error.message}")
                }
                .subscribe()
        }

    }

}
