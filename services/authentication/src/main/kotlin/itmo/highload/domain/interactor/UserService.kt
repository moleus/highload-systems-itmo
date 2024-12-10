package itmo.highload.domain.interactor

import itmo.highload.domain.UserRepository
import itmo.highload.infrastructure.http.dto.RegisterDto
import itmo.highload.infrastructure.postgres.model.Users
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {

    fun getByLogin(login: String): Mono<Users> = userRepository.findByLogin(login)
        .switchIfEmpty(Mono.error(NoSuchElementException("User with login $login not found")))

    fun addUser(request: RegisterDto): Mono<Users> {
        val users = Users(
            login = request.login,
            password = encoder.encode(request.password),
            role = request.role!!,
            creationDate = LocalDate.now()
        )
        return userRepository.save(users)
    }

    fun checkIfExists(login: String): Mono<Boolean> = userRepository.findByLogin(login).hasElement()
}
