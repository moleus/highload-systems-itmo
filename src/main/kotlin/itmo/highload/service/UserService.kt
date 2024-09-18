package itmo.highload.service

import itmo.highload.repository.UserRepository
import itmo.highload.controller.request.RegisterRequest
import itmo.highload.model.User
import lombok.RequiredArgsConstructor
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
@RequiredArgsConstructor
class UserService(val userRepository: UserRepository) {

    @Throws(UsernameNotFoundException::class)
    fun getByLogin(login: String): User {
        val user: User? = userRepository.findByLogin(login)
        return user ?: throw UsernameNotFoundException("User not found")
    }

    fun addUser(request: RegisterRequest): User {
        val user = User(
            login = request.login,
            password = request.password,
            role = request.role
        )

        return userRepository.save(user)
    }

    fun checkIfExists(login: String): Boolean {
        val dbUser: User? = userRepository.findByLogin(login)
        return dbUser != null
    }
}
