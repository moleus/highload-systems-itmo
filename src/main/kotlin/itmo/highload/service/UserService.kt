package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.User
import itmo.highload.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

const val DEMO_SUPERUSER_LOGIN = "superuser"
const val DEMO_CUSTOMER_LOGIN = "customer"
const val DEMO_EXPENSE_MANAGER_LOGIN = "emanager"
const val DEMO_ADOPTION_MANAGER_LOGIN = "amanager"

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {

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
