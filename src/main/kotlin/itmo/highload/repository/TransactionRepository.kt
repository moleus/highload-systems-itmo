package itmo.highload.repository

import itmo.highload.model.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {
    fun findByIsDonation(isDonation: Boolean, pageable: Pageable): Page<Transaction>
    fun findByIsDonationAndBalanceId(isDonation: Boolean, balanceId: Int, pageable: Pageable): Page<Transaction>
    fun findByIsDonationAndUserId(isDonation: Boolean, userId: Int, pageable: Pageable): Page<Transaction>
}
