package itmo.highload.service

import itmo.highload.model.User
import itmo.highload.repository.UserRepository
import org.springframework.stereotype.Service

const val DEMO_SUPERUSER_LOGIN = "superuser"
const val DEMO_CUSTOMER_LOGIN = "customer"
const val DEMO_EXPENSE_MANAGER_LOGIN = "emanager"
const val DEMO_ADOPTION_MANAGER_LOGIN = "amanager"

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getByLogin(login: String): User {
        return userRepository.findByLogin(login) ?: throw NoSuchElementException("User with login $login not found")
    }
}
