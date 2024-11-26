package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.Balance
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface BalanceRepository {
    fun findByPurpose(purpose: String): Mono<Balance>
    fun save(balance: Balance): Mono<Balance>
    fun findById(id: Int): Mono<Balance>
    fun findAll(): Flux<Balance>
}
