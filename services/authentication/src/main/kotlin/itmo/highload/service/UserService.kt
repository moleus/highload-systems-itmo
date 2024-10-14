package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.Users
import itmo.highload.repository.UserRepository
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
