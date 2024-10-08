package itmo.highload.repository

import itmo.highload.model.Balance
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BalanceRepository : ReactiveCrudRepository<Balance, Int> {
    fun findByPurpose(purpose: String): Mono<Balance>
}
