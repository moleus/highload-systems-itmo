package itmo.highload.controller

import itmo.highload.dto.LoginDto
import itmo.highload.dto.RegisterDto
import itmo.highload.security.dto.JwtResponse
import itmo.highload.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginDto): Mono<JwtResponse> =
        authService.login(request.login, request.password).onErrorMap { e ->
            ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message, e)
        }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterDto): Mono<String> =
        authService.checkIfUserExists(request.login).flatMap { exists ->
            if (exists) {
                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"))
            } else {
                authService.register(request).map { it.role.toString() }
            }
        }
}
