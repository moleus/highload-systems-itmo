package itmo.highload

import io.mockk.every
import io.mockk.mockk
import itmo.highload.dto.RegisterDto
import itmo.highload.model.Users
import itmo.highload.repository.UserRepository
import itmo.highload.security.Role
import itmo.highload.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var encoder: PasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        encoder = mockk()
        userService = UserService(userRepository, encoder)
    }

    @Test
    fun `getByLogin - should return user if found`() {
        val user = Users(
            id = 1,
            login = "testUser",
            password = "hashedPassword",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )

        every { userRepository.findByLogin("testUser") } returns Mono.just(user)

        StepVerifier.create(userService.getByLogin("testUser"))
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `getByLogin - should throw error if user not found`() {
        every { userRepository.findByLogin("nonexistentUser") } returns Mono.empty()

        StepVerifier.create(userService.getByLogin("nonexistentUser"))
            .expectErrorMatches { it is NoSuchElementException
                    && it.message == "User with login nonexistentUser not found" }
            .verify()
    }

    @Test
    fun `addUser - should save user to repository`() {
        val request = RegisterDto(login = "newUser", password = "password", role = Role.CUSTOMER)
        val savedUser = Users(
            id = 1,
            login = "newUser",
            password = "hashedPassword",
            role = Role.CUSTOMER,
            creationDate = LocalDate.now()
        )

        every { encoder.encode(request.password) } returns "hashedPassword"
        every { userRepository.save(any()) } returns Mono.just(savedUser)

        StepVerifier.create(userService.addUser(request))
            .expectNext(savedUser)
            .verifyComplete()
    }

    @Test
    fun `checkIfExists - should return true if user exists`() {
        every { userRepository.findByLogin("existingUser") } returns Mono.just(
            Users(
                id = 1,
                login = "existingUser",
                password = "password",
                role = Role.CUSTOMER,
                creationDate = LocalDate.now()
            )
        )

        StepVerifier.create(userService.checkIfExists("existingUser"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `checkIfExists - should return false if user does not exist`() {
        every { userRepository.findByLogin("nonexistentUser") } returns Mono.empty()

        StepVerifier.create(userService.checkIfExists("nonexistentUser"))
            .expectNext(false)
            .verifyComplete()
    }
}
