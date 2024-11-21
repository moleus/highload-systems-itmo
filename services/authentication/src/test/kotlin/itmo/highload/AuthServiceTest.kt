package itmo.highload

import io.mockk.every
import io.mockk.mockk
import itmo.highload.dto.RegisterDto
import itmo.highload.model.Users
import itmo.highload.security.Role
import itmo.highload.security.dto.JwtResponse
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.AuthService
import itmo.highload.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

class AuthServiceTest {

    private lateinit var jwtProvider: JwtUtils
    private lateinit var userService: UserService
    private lateinit var encoder: PasswordEncoder
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        jwtProvider = mockk()
        userService = mockk()
        encoder = mockk()
        authService = AuthService(jwtProvider, userService, encoder)
    }

    @Test
    fun `login - should return JWT token on success`() {
        val user = Users(
            id = 1,
            login = "testUser",
            password = "hashedPassword",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )
        val rawPassword = "password"
        val accessToken = "fakeToken"

        every { userService.getByLogin("testUser") } returns Mono.just(user)
        every { encoder.matches(rawPassword, user.password) } returns true
        every { jwtProvider.generateAccessToken(user.login, user.role, user.id) } returns accessToken

        StepVerifier.create(authService.login("testUser", rawPassword))
            .expectNextMatches { jwtResponse ->
                jwtResponse.accessToken == accessToken && jwtResponse.role == user.role
            }
            .verifyComplete()
    }

    @Test
    fun `login - should throw BadCredentialsException for incorrect password`() {
        val user = Users(
            id = 1, login = "testUser", password = "hashedPassword", role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )
        val rawPassword = "wrongPassword"

        every { userService.getByLogin("testUser") } returns Mono.just(user)
        every { encoder.matches(rawPassword, user.password) } returns false

        StepVerifier.create(authService.login("testUser", rawPassword))
            .expectErrorMatches { it is BadCredentialsException && it.message == "Wrong password" }
            .verify()
    }

    @Test
    fun `register - should add new user`() {
        val request = RegisterDto(login = "newUser", password = "password", role = Role.CUSTOMER)
        val newUser = Users(
            id = 1,
            login = "newUser",
            password = "hashedPassword",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )

        every { userService.addUser(request) } returns Mono.just(newUser)

        StepVerifier.create(authService.register(request))
            .expectNext(newUser)
            .verifyComplete()
    }

    @Test
    fun `checkIfUserExists - should return true if user exists`() {
        every { userService.checkIfExists("existingUser") } returns Mono.just(true)

        StepVerifier.create(authService.checkIfUserExists("existingUser"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `checkIfUserExists - should return false if user does not exist`() {
        every { userService.checkIfExists("nonexistentUser") } returns Mono.just(false)

        StepVerifier.create(authService.checkIfUserExists("nonexistentUser"))
            .expectNext(false)
            .verifyComplete()
    }
}
