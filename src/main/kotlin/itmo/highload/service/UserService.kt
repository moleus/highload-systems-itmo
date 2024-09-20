package itmo.highload.service

import itmo.highload.controller.request.RegisterRequest
import itmo.highload.model.User
import itmo.highload.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {

    @Throws(UsernameNotFoundException::class)
    fun getByLogin(login: String): User {
        return userRepository.findByLogin(login) ?: throw UsernameNotFoundException("User not found")
    }

    fun addUser(request: RegisterRequest): User {
        val user = User(
            login = request.login,
            password = encoder.encode(request.password),
            role = request.role,
            createdDate = LocalDate.now()
        )

        return userRepository.save(user)
    }

    fun checkIfExists(login: String): Boolean {
        return userRepository.findByLogin(login) != null
    }
}
