@file:Suppress("LineLength")
package itmo.highload.repository

import itmo.highload.model.Transaction
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TransactionRepository : R2dbcRepository<Transaction, Int> {
    @Suppress("MaxLineLength")
    @Query("SELECT * FROM transaction WHERE is_donation = :isDonation LIMIT :pageable.pageSize OFFSET :pageable.offset")
    fun findByIsDonation(isDonation: Boolean, pageable: Pageable): Flux<Transaction>

    @Suppress("MaxLineLength")
    @Query("SELECT * FROM transaction WHERE is_donation = :isDonation AND balance_id = :balanceId LIMIT :pageable.pageSize OFFSET :pageable.offset")
    fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int, pageable: Pageable): Flux<Transaction>

    @Suppress("MaxLineLength")
    @Query("SELECT * FROM transaction WHERE is_donation = :isDonation AND user_id = :userId LIMIT :pageable.pageSize OFFSET :pageable.offset")
    fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int, pageable: Pageable): Flux<Transaction>
}
