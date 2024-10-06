package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.User
import itmo.highload.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {

    fun getByLogin(login: String): User {
        return userRepository.findByLogin(login) ?: throw NoSuchElementException("User with login $login not found")
    }

    fun addUser(request: RegisterDto): User {
        val user = User(
            login = request.login,
            password = encoder.encode(request.password),
            role = request.role,
            creationDate = LocalDate.now()
        )

        return userRepository.save(user)
    }

    fun checkIfExists(login: String): Boolean {
        return userRepository.findByLogin(login) != null
    }
}
