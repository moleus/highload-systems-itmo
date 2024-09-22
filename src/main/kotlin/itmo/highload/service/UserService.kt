package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.User
import itmo.highload.model.enum.UserRole
import itmo.highload.repository.UserRepository
import jakarta.annotation.PostConstruct
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

    @PostConstruct
    fun initializeUsers() {
        if (!checkIfExists("superuser")) {
            val user = User(
                login = "superuser",
                password = encoder.encode("123"),
                role = UserRole.SUPERUSER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
        }
        if (!checkIfExists("customer")) {
            val user = User(
                login = "customer",
                password = encoder.encode("123"),
                role = UserRole.CUSTOMER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
        }
        if (!checkIfExists("emanager")) {
            val user = User(
                login = "emanager",
                password = encoder.encode("123"),
                role = UserRole.EXPENSE_MANAGER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
        }
        if (!checkIfExists("amanager")) {
            val user = User(
                login = "amanager",
                password = encoder.encode("123"),
                role = UserRole.ADOPTION_MANAGER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
        }
    }

    @Throws(UsernameNotFoundException::class)
    fun getByLogin(login: String): User {
        return userRepository.findByLogin(login) ?: throw UsernameNotFoundException("User not found")
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
