package itmo.highload.service

import itmo.highload.dto.RegisterDto
import itmo.highload.model.User
import itmo.highload.security.dto.JwtResponse
import itmo.highload.security.jwt.TokenUtils
import jakarta.security.auth.message.AuthException
import org.springframework.context.annotation.Profile
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Profile("security")
@Service
class AuthService(
    private val jwtProvider: TokenUtils,
    private val userService: UserService,
    private val encoder: PasswordEncoder
) {

    @Throws(AuthException::class)
    fun login(login: String, password: String): JwtResponse {
        try {
            val user: User = userService.getByLogin(login)

            if (!encoder.matches(password, user.password)) {
                throw AuthException("Wrong password")
            }

            val accessToken: String = jwtProvider.generateAccessToken(user.login, user.role)
            return JwtResponse(accessToken, user.role)

        } catch (e: UsernameNotFoundException) {
            throw AuthException("User not found", e)
        }
    }

    fun register(request: RegisterDto): User {
        return userService.addUser(request)
    }

    fun checkIfUserExists(login: String): Boolean {
        return userService.checkIfExists(login)
    }
}
