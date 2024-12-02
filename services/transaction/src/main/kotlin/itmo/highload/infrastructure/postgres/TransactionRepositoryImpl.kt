@file:Suppress("LineLength")
package itmo.highload.infrastructure.postgres

import itmo.highload.domain.TransactionRepository
import itmo.highload.infrastructure.postgres.model.Transaction
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TransactionRepositoryImpl : R2dbcRepository<Transaction, Int>, TransactionRepository {
    override fun findByIsDonation(isDonation: Boolean): Flux<Transaction>
    override fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int): Flux<Transaction>
    override fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int): Flux<Transaction>
    @Query("UPDATE transaction SET status = :status WHERE id = :transactionId RETURNING *")
    override fun updateStatus(transactionId: Int, status: String): Mono<Transaction>
}
