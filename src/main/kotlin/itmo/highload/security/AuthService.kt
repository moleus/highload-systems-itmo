package itmo.highload.security

import itmo.highload.controller.request.RegisterRequest
import itmo.highload.controller.response.JwtResponse
import itmo.highload.model.User
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.service.UserService
import jakarta.security.auth.message.AuthException
import lombok.RequiredArgsConstructor
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class AuthService(val jwtProvider: JwtProvider,
                  val userService: UserService,
                  val encoder: PasswordEncoder) {

    @Throws(AuthException::class)
    fun login(login: String, password: String): JwtResponse {
        try {
            val user: User = userService.getByLogin(login)

            if (encoder.matches(password, user.password)) {
                val accessToken: String = jwtProvider.generateAccessToken(user)
                val refreshToken: String = jwtProvider.generateRefreshToken(user)
                return JwtResponse(accessToken, refreshToken, user.role)
            }
            throw AuthException("Wrong password")
        } catch (e: UsernameNotFoundException) {
            throw AuthException("User not found")
        }
    }

    fun register(request: RegisterRequest): User {
        return userService.addUser(request)
    }

    @Throws(AuthException::class)
    fun getNewAccessToken(refreshToken: String): JwtResponse {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            val username: String = jwtProvider.getRefreshClaims(refreshToken).subject
            val dbUser: User = userService.getByLogin(username)
            val newAccessToken: String = jwtProvider.generateAccessToken(dbUser)
            return JwtResponse(newAccessToken, null, dbUser.role)
        }
        throw AuthException("Invalid refresh JWT token")
    }

    @Throws(AuthException::class)
    fun refresh(refreshToken: String): JwtResponse {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            val username: String = jwtProvider.getRefreshClaims(refreshToken).subject
            val dbUser: User = userService.getByLogin(username)
            val newAccessToken: String = jwtProvider.generateAccessToken(dbUser)
            val newRefreshToken: String = jwtProvider.generateRefreshToken(dbUser)
            return JwtResponse(newAccessToken, newRefreshToken, dbUser.role)
        }
        throw AuthException("Invalid refresh JWT token")
    }

    fun checkIfUserExists(login: String): Boolean {
        return userService.checkIfExists(login)
    }
}
