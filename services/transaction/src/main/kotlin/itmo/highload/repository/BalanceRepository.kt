package itmo.highload.repository

import itmo.highload.model.Balance
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BalanceRepository : R2dbcRepository<Balance, Int> {
    fun findByPurpose(purpose: String): Mono<Balance>
}
