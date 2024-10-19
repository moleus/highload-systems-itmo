package itmo.highload.repository

import itmo.highload.model.Users
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : R2dbcRepository<Users, Int> {
    @Query("SELECT * FROM users WHERE login = :login")
    fun findByLogin(login: String): Mono<Users>

    fun save(users: Users): Mono<Users>
}
