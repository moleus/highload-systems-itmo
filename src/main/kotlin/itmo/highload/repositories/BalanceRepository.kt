package itmo.highload.repositories

import itmo.highload.model.Balance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BalanceRepository : JpaRepository<Balance, Int> {
    fun findByPurpose(purpose: String): Balance?
}
