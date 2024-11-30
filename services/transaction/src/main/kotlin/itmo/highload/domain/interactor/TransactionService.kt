package itmo.highload.domain.interactor

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.TransactionProducer
import itmo.highload.domain.TransactionRepository
import itmo.highload.domain.mapper.TransactionMapper
import itmo.highload.kafka.TransactionResultMessage
import jakarta.persistence.EntityNotFoundException
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
    private val hazelcastInstance: HazelcastInstance,
    @Value("\${transaction.delay}")
    val delay: Long
) {
    private val logger = KotlinLogging.logger {}

    private val transactionCacheName = "transaction"

    private val balanceCacheName = "balance"

    private var isCacheInitialized = false

    fun getExpenses(purposeId: Int?, token: String): Flux<TransactionResponse> {
        val cache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        if (!isCacheInitialized) {
            initializeTransactionCache(token, cache)
            isCacheInitialized = true
        }

        logger.info { "Getting expenses, purposeId: {$purposeId}" }

        val cachedResults = if (purposeId != null) {
            Flux.fromIterable(cache.values)
                .filter { !it.isDonation && it.purpose.id == purposeId }
        } else {
            Flux.fromIterable(cache.values)
                .filter { !it.isDonation }
        }

        return cachedResults.switchIfEmpty(
            fetchExpensesFromDatabase(purposeId, token)
                .doOnNext { response ->
                    cache[response.id] = response
                    logger.info { "Added transaction ${response.id} to cache" }
                }
        )
    }

    fun getDonations(purposeId: Int?, token: String): Flux<TransactionResponse> {
        val transactionCache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        if (!isCacheInitialized) {
            initializeTransactionCache(token, transactionCache)
            isCacheInitialized = true
        }

        logger.info { "Getting donations, purposeId: {$purposeId}" }

        val cachedResults = if (purposeId != null) {
            Flux.fromIterable(transactionCache.values)
                .filter { it.isDonation && it.purpose.id == purposeId }
        } else {
            Flux.fromIterable(transactionCache.values)
                .filter { it.isDonation }
        }

        return cachedResults.switchIfEmpty(
            fetchDonationsFromDatabase(purposeId, token)
                .doOnNext { response ->
                    transactionCache[response.id] = response
                    logger.info { "Added donation transaction ${response.id} to cache" }
                }
        )
    }

    private fun fetchDonationsFromDatabase(purposeId: Int?, token: String): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(true, purposeId).flatMap { transaction ->
                logger.info { "Fetching donation from database: $transaction" }
                getBalanceCached(transaction.balanceId, token)
                    .map { balance ->
                        TransactionMapper.toResponse(transaction, balance)
                    }
            }
        } else {
            transactionRepository.findByIsDonation(true).flatMap { transaction ->
                logger.info { "Fetching donation from database: $transaction" }
                getBalanceCached(transaction.balanceId, token)
                    .map { balance ->
                        TransactionMapper.toResponse(transaction, balance)
                    }
            }
        }
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, token: String): Flux<TransactionResponse> {
        val transactionCache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        if (!isCacheInitialized) {
            initializeTransactionCache(token, transactionCache)
            isCacheInitialized = true
        }

        logger.info { "Getting all transactions for userId: $userId, isDonation: $isDonation" }

        val cachedResults = Flux.fromIterable(transactionCache.values)
            .filter { it.isDonation == isDonation && it.userId == userId }

        return cachedResults.switchIfEmpty(
            fetchUserTransactionsFromDatabase(isDonation, userId, token)
                .doOnNext { response ->
                    transactionCache[response.id] = response
                    logger.info { "Added user transaction ${response.id} to cache" }
                }
        )
    }

    private fun fetchUserTransactionsFromDatabase(
        isDonation: Boolean,
        userId: Int,
        token: String
    ): Flux<TransactionResponse> {
        return transactionRepository.findByIsDonationAndUserId(isDonation, userId).flatMap { transaction ->
            logger.info { "Fetching user transaction from database: $transaction" }
            getBalanceCached(transaction.balanceId, token)
                .map { balance ->
                    TransactionMapper.toResponse(transaction, balance)
                }
        }
    }

    private fun getBalanceCached(balanceId: Int, token: String): Mono<BalanceResponse> {
        val balanceCache = hazelcastInstance.getMap<Int, BalanceResponse>(balanceCacheName)
        val cachedBalance = balanceCache[balanceId]

        return if (cachedBalance != null) {
            logger.info { "Found balance in cache for balanceId: $balanceId" }
            Mono.just(cachedBalance)
        } else {
            balanceService.getBalanceById(token, balanceId)
                .doOnNext { balance ->
                    balanceCache[balanceId] = balance
                    logger.info { "Added balance for balanceId $balanceId to cache" }
                }
        }
    }


    fun addTransaction(
        donationDto: TransactionDto,
        managerId: Int,
        isDonation: Boolean
    ): Mono<TransactionResponse> {
        val transactionEntity = TransactionMapper.toEntityFromTransactionDTO(donationDto, managerId, isDonation)
        logger.info { "Adding transaction: $transactionEntity" }
        val transactionCache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        return transactionRepository.save(transactionEntity)
            .flatMap { savedTransaction ->
                val message = TransactionMapper.toBalanceMessage(savedTransaction)
                Mono.fromCallable {
                    logger.info { "Sending transaction check message to Kafka" }
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
                val response = TransactionMapper.toResponseFromTransaction(savedTransaction)
                transactionCache[savedTransaction.id] = response
                response
            }
    }

    fun rollbackTransaction(transactionId: Int): Mono<Void> {
        val transactionCache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        return transactionRepository.updateStatus(transactionId, "CANCELED")
            .doOnSubscribe {
                logger.info { "Rolling back transaction $transactionId due to Kafka sending failure." }
            }
            .doOnSuccess {
                transactionCache.computeIfPresent(transactionId) { _, cachedTransaction ->
                    cachedTransaction.copy(status = "CANCELED")
                }
                logger.warn { "Transaction $transactionId successfully rolled back." }
            }
            .doOnError { error ->
                logger.error { "Failed to rollback transaction $transactionId: ${error.message}" }
            }
            .then()
    }

    fun confirmTransaction(transaction: TransactionResultMessage): Mono<Void> {
        val transactionCache = hazelcastInstance.getMap<Int, TransactionResponse>(transactionCacheName)
        return Mono.defer {
            logger.info { "Starting to confirm transaction ${transaction.transactionId}." }
            Mono.delay(Duration.ofSeconds(delay))
                .then(
                    transactionRepository.updateStatus(transaction.transactionId, "COMPLETED")
                        .doOnSubscribe {
                            logger.info { "Confirming transaction ${transaction.transactionId} " +
                                    "due to Kafka sending success." }
                        }
                        .doOnSuccess {
                            transactionCache.computeIfPresent(transaction.transactionId) { _, cachedTransaction ->
                                cachedTransaction.copy(status = "COMPLETED")
                            }
                            logger.info { "Transaction ${transaction.transactionId} successfully confirmed." }
                        }
                        .doOnError { error ->
                            rollbackTransaction(transaction.transactionId)
                                .doOnTerminate {
                                    transactionProducer.sendRollBackMessage(
                                        TransactionMapper.toTransactionRollBackMessageFromResultMessage(transaction)
                                    )
                                    transactionCache.computeIfPresent(transaction.transactionId) { _, cachedTransaction ->
                                        cachedTransaction.copy(status = "CANCELED")
                                    }
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

    private fun initializeTransactionCache(token: String, transactionCache: IMap<Int, TransactionResponse>) {
        val balanceCache = hazelcastInstance.getMap<Int, BalanceResponse>(balanceCacheName)

        transactionRepository.findAll()
            .flatMap { transaction ->
                val cachedBalance = balanceCache[transaction.balanceId]

                if (cachedBalance != null) {
                    Mono.just(cachedBalance).map { balance ->
                        val response = TransactionMapper.toResponse(transaction, balance)
                        transactionCache[transaction.id] = response
                        response
                    }
                } else {
                    balanceService.getBalanceById(token, transaction.balanceId)
                        .doOnNext { balance ->
                            balanceCache[transaction.balanceId] = balance
                            logger.info { "Balance for ID ${transaction.balanceId} added to cache" }
                        }
                        .map { balance ->
                            val response = TransactionMapper.toResponse(transaction, balance)
                            transactionCache[transaction.id] = response
                            response
                        }
                }
            }
            .subscribe({
                logger.info { "Transaction with date ${it.dateTime} added to cache" }
            }, {
                logger.error { "Failed to initialize transaction cache: ${it.message}" }
            })
    }


    private fun fetchExpensesFromDatabase(purposeId: Int?, token: String): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(false, purposeId).flatMap { transaction ->
                logger.info { "Fetching transaction from database: $transaction" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        TransactionMapper.toResponse(transaction, balance)
                    }
            }
        } else {
            transactionRepository.findByIsDonation(false).flatMap { transaction ->
                logger.info { "Fetching transaction from database: $transaction" }
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance ->
                        TransactionMapper.toResponse(transaction, balance)
                    }
            }
        }
    }
}


