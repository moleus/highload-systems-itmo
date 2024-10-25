package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
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
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = [Content(schema = Schema(implementation = JwtResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized access")
        ]
    )
    fun login(@RequestBody @Valid request: LoginDto): Mono<JwtResponse> =
        authService.login(request.login, request.password).onErrorMap { e ->
            ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message, e)
        }


    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @Operation(summary = "Token", description = "Authenticate using form-encoded username and password, returning JWT.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = [Content(schema = Schema(implementation = JwtResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized access")
        ]
    )
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
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(responseCode = "409", description = "Conflict: Username already exists"),
            ApiResponse(responseCode = "403", description = "Forbidden: No authority for this operation")
        ]
    )
    fun register(@RequestBody @Valid request: RegisterDto): Mono<String> =
        authService.checkIfUserExists(request.login).flatMap { exists ->
            if (exists) {
                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"))
            } else {
                authService.register(request).map { it.role.toString() }
            }
        }
}
