package itmo.highload

import io.mockk.every
import io.mockk.mockk
import itmo.highload.domain.interactor.AuthService
import itmo.highload.infrastructure.http.AuthController
import itmo.highload.infrastructure.http.dto.LoginDto
import itmo.highload.infrastructure.http.dto.RegisterDto
import itmo.highload.infrastructure.postgres.model.Users
import itmo.highload.security.Role
import itmo.highload.security.dto.JwtResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

class AuthControllerTest {

    private lateinit var authService: AuthService
    private lateinit var authController: AuthController


    @BeforeEach
    fun setup() {
        authService = mockk()
        authController = AuthController(authService)
    }

    @Test
    fun `login - should return JWT token on success`() {
        val loginDto = LoginDto(login = "user", password = "password")
        val jwtResponse = JwtResponse("fakeToken", role = Role.CUSTOMER)

        every { authService.login(loginDto.login, loginDto.password) } returns Mono.just(jwtResponse)

        StepVerifier.create(authController.login(loginDto))
            .expectNext(jwtResponse)
            .verifyComplete()
    }

    @Test
    fun `login - should return UNAUTHORIZED on error`() {
        val loginDto = LoginDto(login = "user", password = "wrongPassword")

        every {
            authService.login(
                loginDto.login,
                loginDto.password
            )
        } returns Mono.error(Exception("Invalid credentials"))

        StepVerifier.create(authController.login(loginDto))
            .expectErrorMatches { e ->
                e is ResponseStatusException && e.statusCode == HttpStatus.UNAUTHORIZED &&
                        e.reason == "Invalid credentials"
            }
            .verify()
    }

    @Test
    fun `token - should return JWT token on success`() {
        val exchange = mockk<ServerWebExchange>()
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("username", "user")
        formData.add("password", "password")

        val jwtResponse = JwtResponse("fakeToken", role = Role.CUSTOMER)

        every { exchange.formData } returns Mono.just(formData)
        every { authService.login("user", "password") } returns Mono.just(jwtResponse)

        StepVerifier.create(authController.token(exchange))
            .expectNext(jwtResponse)
            .verifyComplete()
    }


    @Test
    fun `token - should return UNAUTHORIZED on error`() {
        val exchange = mockk<ServerWebExchange>()
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("username", "user")
        formData.add("password", "password")

        every { exchange.formData } returns Mono.just(formData)
        every { authService.login("user", "wrongPassword") } returns Mono.error(Exception("Invalid " +
                "credentials"))

        StepVerifier.create(authController.token(exchange))
            .expectErrorMatches { e ->
                e is ResponseStatusException && e.statusCode == HttpStatus.UNAUTHORIZED
            }
            .verify()
    }

    @Test
    fun `register - should register user when username does not exist`() {
        val registerDto = RegisterDto(login = "newUser", password = "password", role = Role.CUSTOMER)
        val registeredUser = Users(
            id = 1,
            login = "newUser",
            role = Role.CUSTOMER,
            password = "password",
            creationDate = LocalDate.of(2021, 10, 10)
        )

        every { authService.checkIfUserExists(registerDto.login) } returns Mono.just(false)
        every { authService.register(registerDto) } returns Mono.just(registeredUser)

        StepVerifier.create(authController.register(registerDto))
            .expectNext("CUSTOMER")
            .verifyComplete()
    }

    @Test
    fun `register - should return CONFLICT when username already exists`() {
        val registerDto = RegisterDto(login = "existingUser", password = "password", role = Role.CUSTOMER)

        every { authService.checkIfUserExists(registerDto.login) } returns Mono.just(true)

        StepVerifier.create(authController.register(registerDto))
            .expectErrorMatches { e ->
                e is ResponseStatusException && e.statusCode == HttpStatus.CONFLICT && e.reason == "Username " +
                        "already exists"
            }
            .verify()
    }

}
