package itmo.highload.repository

import itmo.highload.model.Transaction
import itmo.highload.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {
    fun findByUser(user: User, pageable: Pageable): Page<Transaction>
    fun findByIsDonation(isDonation: Boolean, pageable: Pageable): Page<Transaction>
}
