package itmo.highload.repositories

import itmo.highload.model.Transaction
import itmo.highload.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {
    fun findByUser(user: User): List<Transaction>
    fun findByIsDonation(isDonation: Boolean): List<Transaction>
}