package itmo.highload.infrastructure.postgres

import itmo.highload.domain.BalanceRepository
import itmo.highload.infrastructure.postgres.model.Balance
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BalanceRepositoryImpl : R2dbcRepository<Balance, Int>, BalanceRepository {
    override fun findByPurpose(purpose: String): Mono<Balance>
}
