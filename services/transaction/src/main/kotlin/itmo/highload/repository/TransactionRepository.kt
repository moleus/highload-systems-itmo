@file:Suppress("LineLength")
package itmo.highload.repository

import itmo.highload.model.Transaction
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TransactionRepository : ReactiveCrudRepository<Transaction, Int> {
    fun findByIsDonation(isDonation: Boolean, pageable: Pageable): Flux<Transaction>
    fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int, pageable: Pageable): Flux<Transaction>
    fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int, pageable: Pageable): Flux<Transaction>
}
