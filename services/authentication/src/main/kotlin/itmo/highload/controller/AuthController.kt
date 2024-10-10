package itmo.highload.controller

import itmo.highload.dto.LoginDto
import itmo.highload.dto.RegisterDto
import itmo.highload.model.User
import itmo.highload.security.dto.JwtResponse
import itmo.highload.service.AuthService
import jakarta.security.auth.message.AuthException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("\${app.base-url}/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginDto): JwtResponse {
        return try {
            authService.login(request.login, request.password)
        } catch (e: AuthException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message, e)
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterDto): String {
        if (authService.checkIfUserExists(request.login)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
        }
        val newUser: User = authService.register(request)
        return newUser.role.toString()
    }
}
