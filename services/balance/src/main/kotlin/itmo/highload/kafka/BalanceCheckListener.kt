package itmo.highload.kafka

import itmo.highload.service.BalanceService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BalanceCheckListener(
    private val kafkaTemplate: KafkaTemplate<String, TransactionResultMessage>,
    private val balanceService: BalanceService
) {

    private val logger = LoggerFactory.getLogger(BalanceCheckListener::class.java)

    @KafkaListener(topics = ["\${spring.kafka.consumer.balance-change-topic}"], groupId = "balance_change_group")
    fun listenToBalanceCheckTopic(@Payload message: TransactionBalanceMessage) {
        balanceService.checkAndAdjustBalance(message.balanceId, message.isDonation, message.moneyAmount)
            .doOnSuccess { success ->
                val resultMessage = TransactionResultMessage(
                    dateTime = LocalDateTime.now(),
                    transactionId = message.transactionId,
                    balanceId = message.balanceId,
                    moneyAmount = message.moneyAmount,
                    success = success,
                    message = if (success) "Transaction successful" else "Insufficient balance"
                )
                logger.info("result: $resultMessage")

                kafkaTemplate.send("transaction_result", resultMessage)
                logger.info("Sent transaction result for ${message.transactionId}: success=$success")
            }
            .doOnError { error ->
                logger.error("Error processing transaction for ${message.transactionId}: ${error.message}", error)

                val resultMessage = TransactionResultMessage(
                    dateTime = LocalDateTime.now(),
                    transactionId = message.transactionId,
                    moneyAmount = message.moneyAmount,
                    balanceId = message.balanceId,
                    success = false,
                    message = error.message ?: "Transaction failed"
                )

                kafkaTemplate.send("transaction_result", resultMessage)
            }
            .onErrorReturn(false)
            .subscribe()
    }

    @KafkaListener(topics = ["\${spring.kafka.consumer.roll-back-topic}"], groupId = "balance_change_group")
    fun listenToRollBackTopic(@Payload message: TransactionBalanceMessage) {
        balanceService.rollbackBalance(message.balanceId, message.isDonation, message.moneyAmount)
            .doOnSuccess {
                logger.info("Rollback successful for ${message.transactionId}")
            }
            .doOnError { error ->
                logger.error("Error rolling back transaction for ${message.transactionId}: ${error.message}", error)
            }
            .onErrorReturn(false)
            .subscribe()
    }
}
