package itmo.highload.security

import io.jsonwebtoken.JwtException
import itmo.highload.dto.RegisterDto
import itmo.highload.dto.response.JwtResponse
import itmo.highload.model.User
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.service.UserService
import jakarta.security.auth.message.AuthException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
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

            val accessToken: String = jwtProvider.generateAccessToken(user)
            val refreshToken: String = jwtProvider.generateRefreshToken(user)
            return JwtResponse(accessToken, refreshToken, user.role)

        } catch (e: UsernameNotFoundException) {
            throw AuthException("User not found", e)
        }
    }

    fun register(request: RegisterDto): User {
        return userService.addUser(request)
    }

    @Throws(AuthException::class)
    fun getNewAccessToken(refreshToken: String): JwtResponse {
        try {
            jwtProvider.validateRefreshToken(refreshToken)
            val username: String = jwtProvider.getRefreshClaims(refreshToken).subject
            val dbUser: User = userService.getByLogin(username)
            val newAccessToken: String = jwtProvider.generateAccessToken(dbUser)
            return JwtResponse(newAccessToken, refreshToken, dbUser.role)

        } catch (e: JwtException) {
            throw AuthException("Invalid refresh JWT token", e)
        }
    }

    @Throws(AuthException::class)
    fun refresh(refreshToken: String): JwtResponse {
        try {
            jwtProvider.validateRefreshToken(refreshToken)
            val username: String = jwtProvider.getRefreshClaims(refreshToken).subject
            val dbUser: User = userService.getByLogin(username)
            val newAccessToken: String = jwtProvider.generateAccessToken(dbUser)
            val newRefreshToken: String = jwtProvider.generateRefreshToken(dbUser)
            return JwtResponse(newAccessToken, newRefreshToken, dbUser.role)

        } catch (e: JwtException) {
            throw AuthException("Invalid refresh JWT token", e)
        }
    }

    fun checkIfUserExists(login: String): Boolean {
        return userService.checkIfExists(login)
    }
}
