package itmo.highload.service

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.kafka.TransactionProducer
import itmo.highload.kafka.TransactionResultMessage
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService,
    private val transactionProducer: TransactionProducer,
    @Value("\${transaction.delay}")
    val delay: Long
) {
    private val logger = KotlinLogging.logger {}

    fun getExpenses(purposeId: Int?, token: String): Flux<TransactionResponse> {
        logger.info { "Getting expenses, purposeId: {$purposeId}" }
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(false, purposeId).flatMap { transaction ->
                logger.info { "Found transaction for expense: {$transaction}" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        val response = TransactionMapper.toResponse(transaction, balance)
                        response
                    }
            }
        } else {
            transactionRepository.findByIsDonation(false).flatMap { transaction ->
                logger.info { "Found transaction for expense: {$transaction}" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        val response = TransactionMapper.toResponse(transaction, balance)
                        response
                    }
            }
        }
    }

    fun getDonations(purposeId: Int?, token: String): Flux<TransactionResponse> {
        logger.info { "Getting donations, purposeId: {$purposeId}" }
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(true, purposeId).flatMap { transaction ->
                logger.info { "Found transaction for donation: {$transaction}" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        val response = TransactionMapper.toResponse(transaction, balance)
                        response
                    }
            }
        } else {
            transactionRepository.findByIsDonation(true).flatMap { transaction ->
                logger.info { "${"Found transaction for donation: {}"} $transaction" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        val response = TransactionMapper.toResponse(transaction, balance)
                        response
                    }
            }
        }
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, token: String): Flux<TransactionResponse> {
        logger.info { "${"Getting all transactions for userId: {}, isDonation: {}"} $userId $isDonation" }
        return transactionRepository.findByIsDonationAndUserId(isDonation, userId).flatMap { transaction ->
            logger.info { "${"Found transaction for user: {}"} $transaction" }
            balanceService.getBalanceById(token, transaction.balanceId)
                .map { balance ->
                    val response = TransactionMapper.toResponse(transaction, balance)
                    response
                }
        }
    }

    fun addTransaction(
        donationDto: TransactionDto,
        managerId: Int,
        isDonation: Boolean
    ): Mono<TransactionResponse> {
        val transactionEntity = TransactionMapper.toEntityFromTransactionDTO(donationDto, managerId, isDonation)

        return transactionRepository.save(transactionEntity)
            .flatMap { savedTransaction ->
                val message = TransactionMapper.toBalanceMessage(savedTransaction)
                Mono.fromCallable {
                    transactionProducer.sendMessageToBalanceCheck(message)
                }
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorContinue { error, _ ->
                        logger.error { "Failed to send transaction check message to Kafka: ${error.message}" }
                        rollbackTransaction(savedTransaction.id)
                            .then(Mono.error<Void>(error))
                    }
                    .thenReturn(savedTransaction)
            }
            .flatMap { savedTransaction ->
                if (isDonation) {
                    val donationMessage = TransactionMapper.toResponseFromTransaction(savedTransaction)
                    Mono.fromCallable {
                        transactionProducer.sendMessageToNewDonationTopic(donationMessage)
                    }
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorContinue { error, _ ->
                            logger.error { "Failed to send donation message to Kafka: ${error.message}" }
                        }
                        .thenReturn(savedTransaction)
                } else {
                    Mono.just(savedTransaction)
                }
            }
            .map { savedTransaction ->
                TransactionMapper.toResponseFromTransaction(savedTransaction)
            }
    }

    fun rollbackTransaction(transactionId: Int): Mono<Void> {
        return transactionRepository.updateStatus(transactionId, "CANCELED")
            .doOnSubscribe {
                logger.info { "Rolling back transaction $transactionId due to Kafka sending failure." }
            }
            .doOnSuccess {
                logger.warn { "Transaction $transactionId successfully rolled back." }
            }
            .doOnError { error ->
                logger.error { "Failed to rollback transaction $transactionId: ${error.message}" }
            }
            .then()
    }

    fun confirmTransaction(transaction: TransactionResultMessage): Mono<Void> {
        return Mono.defer {
            logger.info { "Starting to confirm transaction ${transaction.transactionId}." }  // Логируем начало выполнения
            Mono.delay(Duration.ofSeconds(delay))
                .then(
                    transactionRepository.updateStatus(transaction.transactionId, "COMPLETED")
                        .doOnSubscribe {
                            logger.info { "Confirming transaction ${transaction.transactionId} due to Kafka sending success." }
                        }
                        .doOnSuccess {
                            logger.info { "Transaction ${transaction.transactionId} successfully confirmed." }
                        }
                        .doOnError { error ->
                            rollbackTransaction(transaction.transactionId)
                                .doOnTerminate {
                                    transactionProducer.sendRollBackMessage(
                                        TransactionMapper.toTransactionRollBackMessageFromResultMessage(transaction)
                                    )
                                }
                                .subscribe()

                            logger.error {
                                "Failed to confirm transaction ${transaction.transactionId}: " +
                                        "${error.message}"
                            }
                        }
                )
                .then()
        }
    }

}
