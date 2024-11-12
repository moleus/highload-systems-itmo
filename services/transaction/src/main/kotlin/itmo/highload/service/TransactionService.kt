package itmo.highload.service

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.kafka.TransactionProducer
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService,
    private val transactionProducer: TransactionProducer
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

    fun addTransaction(donationDto: TransactionDto, managerId: Int, isDonation: Boolean, token: String): Mono<TransactionResponse> {
        // Создаем транзакцию без использования balanceService.getBalanceById
        val transactionEntity = TransactionMapper.toEntity(donationDto, managerId, isDonation)

        return transactionRepository.save(transactionEntity) // Сохраняем транзакцию в БД
            .flatMap { savedTransaction ->
                // Создаем сообщение для проверки баланса
                val message = TransactionMapper.toBalanceMessage(savedTransaction)

                // Отправляем сообщение в Kafka для начала саги
                Mono.fromCallable {
                    transactionProducer.sendMessageToBalanceCheck(message) // Отправляем сообщение на проверку баланса
                }
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorContinue { error, _ ->
                        logger.error { "Failed to send transaction check message to Kafka: ${error.message}" }
                        // Если ошибка отправки в Kafka, откатываем транзакцию
                        rollbackTransaction(savedTransaction.id.toLong()) // Приводим id к Long
                            .then(Mono.error<Void>(error)) // Завершаем ошибкой
                    }
                    .thenReturn(savedTransaction) // Возвращаем сохраненную транзакцию
            }
            .map { savedTransaction ->
                // Преобразуем сохраненную транзакцию в TransactionResponse
                val balance = savedTransaction.balanceId // Здесь предполагается, что баланс будет добавлен позже в саге
                TransactionMapper.toResponse(savedTransaction, balance)
            }
    }


    private fun rollbackTransaction(transactionId: Long): Mono<Void> {
        return transactionRepository.rollbackTransaction(transactionId)
            .doOnSuccess {
                logger.warn("Transaction $transactionId rolled back due to Kafka sending failure.")
            }
            .doOnError { error ->
                logger.error("Failed to rollback transaction $transactionId: ${error.message}")
            }
    }
//    fun addTransaction(donationDto: TransactionDto, managerId: Int, isDonation: Boolean): Mono<TransactionResponse> {
//        return balanceService.getById(donationDto.purposeId!!).flatMap { balance ->
//                val transactionEntity = TransactionMapper.toEntity(donationDto, managerId, balance, isDonation)
//                transactionRepository.save(transactionEntity).flatMap { savedTransaction ->
//                        balanceService.changeMoneyAmount(donationDto.purposeId!!, isDonation,
//                        donationDto.moneyAmount!!)
//                            .thenReturn(savedTransaction)
//                    }
//            }.flatMap { transaction ->
//                balanceService.getById(transaction.balanceId)
//                    .map { balance ->
//                        val transactionResponse = TransactionMapper.toResponse(transaction, balance)
//
//                        if (isDonation) {
//                            val message = TransactionMapper.toResponse(transaction, balance)
//                            Mono.fromCallable { transactionProducer.sendMessageToNewDonationTopic(message) }
//                                .subscribeOn(Schedulers.boundedElastic())
//                                .onErrorContinue { error, _ ->
//                                    logger.error("Failed to send donation message to Kafka: ${error.message}")
//                                }
//                                .subscribe()
//                        }
//                        transactionResponse }
//            }
//    }
}

