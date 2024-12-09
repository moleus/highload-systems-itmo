package itmo.highload.infrastructure.postgres

import itmo.highload.domain.UserRepository
import itmo.highload.infrastructure.postgres.model.Users
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepositoryImpl : R2dbcRepository<Users, Int>, UserRepository {
    @Query("SELECT * FROM users WHERE login = :login")
    override fun findByLogin(login: String): Mono<Users>

    override fun save(users: Users): Mono<Users>
}
