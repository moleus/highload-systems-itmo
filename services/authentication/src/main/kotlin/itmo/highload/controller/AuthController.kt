package itmo.highload.controller

import itmo.highload.dto.LoginDto
import itmo.highload.dto.RegisterDto
import itmo.highload.security.dto.JwtResponse
import itmo.highload.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginDto): Mono<JwtResponse> =
        authService.login(request.login, request.password).onErrorMap { e ->
            ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message, e)
        }


    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun token(
        exchange: ServerWebExchange,
    ): Mono<JwtResponse> {
        return exchange.formData.map {
            LoginDto(
                login = it["username"]?.firstOrNull() ?: "",
                password = it["password"]?.firstOrNull() ?: ""
            )
        }.flatMap { login(it) }.onErrorMap {
            ResponseStatusException(HttpStatus.UNAUTHORIZED, it.message, it)
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun register(@RequestBody @Valid request: RegisterDto): Mono<String> =
        authService.checkIfUserExists(request.login).flatMap { exists ->
            if (exists) {
                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"))
            } else {
                authService.register(request).map { it.role.toString() }
            }
        }
}
