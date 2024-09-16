package itmo.highload.controller

import itmo.highload.controller.request.LoginRequest
import itmo.highload.controller.request.RegisterRequest
import itmo.highload.controller.request.JwtRefreshRequest
import itmo.highload.controller.response.JwtResponse
import itmo.highload.model.User
import itmo.highload.security.AuthService
import jakarta.validation.Valid
import jakarta.security.auth.message.AuthException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.login(request.login, request.password)
            return ResponseEntity.ok<Any>(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body<String>(e.message)
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<String> {
        if (authService.checkIfUserExists(request.login)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exist")
        }

        val newUser: User = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser.role.toString())
    }

    @PostMapping("/token")
    fun getNewAccessToken(@RequestBody @Valid request: JwtRefreshRequest): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.getNewAccessToken(request.refreshToken)
            return ResponseEntity.ok<Any>(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body<String>(e.message)
        }
    }

    @PostMapping("/refresh")
    fun getNewRefreshToken(@RequestBody @Valid request: JwtRefreshRequest): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.refresh(request.refreshToken)
            return ResponseEntity.ok<Any>(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body<String>(e.message)
        }
    }
}