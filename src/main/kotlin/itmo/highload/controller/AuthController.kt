package itmo.highload.controller

import itmo.highload.dto.LoginDto
import itmo.highload.dto.RegisterDto
import itmo.highload.dto.response.JwtResponse
import itmo.highload.model.User
import itmo.highload.security.AuthService
import jakarta.security.auth.message.AuthException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("security")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginDto): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.login(request.login, request.password)
            return ResponseEntity.ok(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterDto): ResponseEntity<String> {
        if (authService.checkIfUserExists(request.login)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exist")
        }

        val newUser: User = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser.role.toString())
    }

    @PostMapping("/token")
    fun getNewAccessToken(@RequestBody @NotBlank refreshToken: String): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.getNewAccessToken(refreshToken)
            return ResponseEntity.ok(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }

    @PostMapping("/refresh")
    fun getNewRefreshToken(@RequestBody @NotBlank refreshToken: String): ResponseEntity<*> {
        try {
            val jwtResponse: JwtResponse = authService.refresh(refreshToken)
            return ResponseEntity.ok(jwtResponse)
        } catch (e: AuthException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }
}
