package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.UserRepository
import itmo.highload.domain.interactor.UserService
import itmo.highload.infrastructure.postgres.model.Users
import itmo.highload.security.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import java.time.LocalDate

class UsersServiceTest {
    private val passwordEncoder: PasswordEncoder = mockk()
    private val userRepository: UserRepository = mockk()
    private val userService = UserService(userRepository, passwordEncoder)

    @Test
    fun `should return user when found by login`() {
        val users = Users(
            id = 1,
            login = "manager",
            password = "123",
            role = Role.ADOPTION_MANAGER,
            creationDate = LocalDate.now()
        )

        every { userRepository.findByLogin("manager") } returns Mono.just(users)

        val result = userService.getByLogin("manager").block()

        assertEquals(users, result)
        verify { userRepository.findByLogin("manager") }
    }

    @Test
    fun `should throw NoSuchElementException when user is not found`() {
        val login = "unknownUser"

        every { userRepository.findByLogin(login) } returns Mono.empty()

        val exception = assertThrows<NoSuchElementException> {
            userService.getByLogin(login).block()
        }

        assertEquals("User with login $login not found", exception.message)
        verify { userRepository.findByLogin(login) }
    }
}
