package itmo.highload.kafka

import itmo.highload.api.dto.response.TransactionResponse
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

    @KafkaListener(topics = ["\${spring.kafka.consumer.balance-check-topic}"], groupId = "balance_check_group")
    fun listenToBalanceCheckTopic(@Payload message: TransactionBalanceMessage) {
        balanceService.checkAndAdjustBalance(message.balanceId, message.isDonation, message.moneyAmount)
            .doOnSuccess { success ->
                // Обрабатываем успешный результат и отправляем в Kafka
                val resultMessage = TransactionResultMessage(
                    dateTime = LocalDateTime.now(),
                    transactionId = message.transactionId,
                    success = success
                )

                kafkaTemplate.send("transaction_result", resultMessage)
                logger.info("Sent transaction result for ${message.transactionId}: success=$success")
            }
            .doOnError { error ->
                // Логируем ошибку и отправляем сообщение с успехом = false
                logger.error("Error processing transaction for ${message.transactionId}: ${error.message}", error)

                val resultMessage = TransactionResultMessage(
                    dateTime = LocalDateTime.now(),
                    transactionId = message.transactionId,
                    success = false // Устанавливаем успех = false в случае ошибки
                )

                kafkaTemplate.send("transaction_result", resultMessage)
            }
            .onErrorReturn(false) // Возвращаем false в случае ошибки
            .subscribe() // Не забываем подписаться на Mono
    }
}