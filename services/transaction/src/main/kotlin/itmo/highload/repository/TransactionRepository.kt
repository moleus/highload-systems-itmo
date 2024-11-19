@file:Suppress("LineLength")
package itmo.highload.repository

import itmo.highload.model.Transaction
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TransactionRepository : R2dbcRepository<Transaction, Int> {
    fun findByIsDonation(isDonation: Boolean): Flux<Transaction>
    fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int): Flux<Transaction>
    fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int): Flux<Transaction>

    @Query("UPDATE transaction SET status = :status WHERE id = :transactionId RETURNING *")
    fun updateStatus(transactionId: Int, status: String): Mono<Transaction>
}
