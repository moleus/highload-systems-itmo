package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.Customer
import itmo.highload.model.User
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.Role
import itmo.highload.repository.CustomerRepository
import itmo.highload.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

const val DEMO_SUPERUSER_LOGIN = "superuser"
const val DEMO_CUSTOMER_LOGIN = "customer"
const val DEMO_EXPENSE_MANAGER_LOGIN = "emanager"
const val DEMO_ADOPTION_MANAGER_LOGIN = "amanager"
const val DEMO_PASSWORD = "123"

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val customerRepository: CustomerRepository
) {

    @PostConstruct
    fun initializeUsers() {
        if (!checkIfExists(DEMO_SUPERUSER_LOGIN)) {
            val user = User(
                login = DEMO_SUPERUSER_LOGIN,
                password = encoder.encode(DEMO_PASSWORD),
                role = Role.SUPERUSER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
            customerRepository.save(Customer(gender = Gender.MALE, address = "Moscow", phone = "+79444333141"))
        }
        if (!checkIfExists(DEMO_CUSTOMER_LOGIN)) {
            val user = User(
                login = DEMO_CUSTOMER_LOGIN,
                password = encoder.encode(DEMO_PASSWORD),
                role = Role.CUSTOMER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
            customerRepository.save(Customer(gender = Gender.MALE, address = "Moscow", phone = "+79444333111"))
        }
        if (!checkIfExists(DEMO_EXPENSE_MANAGER_LOGIN)) {
            val user = User(
                login = DEMO_EXPENSE_MANAGER_LOGIN,
                password = encoder.encode(DEMO_PASSWORD),
                role = Role.EXPENSE_MANAGER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
            customerRepository.save(Customer(gender = Gender.MALE, address = "Moscow", phone = "+79444333112"))
        }
        if (!checkIfExists(DEMO_ADOPTION_MANAGER_LOGIN)) {
            val user = User(
                login = DEMO_ADOPTION_MANAGER_LOGIN,
                password = encoder.encode(DEMO_PASSWORD),
                role = Role.ADOPTION_MANAGER,
                creationDate = LocalDate.now()
            )
            userRepository.save(user)
            customerRepository.save(Customer(gender = Gender.MALE, address = "Moscow", phone = "+79444333131"))
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
