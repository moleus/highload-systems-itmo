package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.Transaction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TransactionRepository {
    fun findByIsDonation(isDonation: Boolean): Flux<Transaction>
    fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int): Flux<Transaction>
    fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int): Flux<Transaction>
    fun updateStatus(transactionId: Int, status: String): Mono<Transaction>
    fun save(transaction: Transaction): Mono<Transaction>
    fun findAll(): Flux<Transaction>
}

