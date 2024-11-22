package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.Users
import reactor.core.publisher.Mono

interface UserRepository {
    fun findByLogin(login: String): Mono<Users>
    fun save(users: Users): Mono<Users>
}
