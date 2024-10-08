package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.model.User
import itmo.highload.model.enum.Role
import itmo.highload.repository.UserRepository
import itmo.highload.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val userService = UserService(userRepository)

    @Test
    fun `should return user when found by login`() {
        val user = User(
            id = 1,
            login = "manager",
            password = "123",
            role = Role.ADOPTION_MANAGER,
            creationDate = LocalDate.now()
        )

        every { userRepository.findByLogin("manager") } returns user

        val result = userService.getByLogin("manager")

        assertEquals(user, result)
        verify { userRepository.findByLogin("manager") }
    }

    @Test
    fun `should throw NoSuchElementException when user is not found`() {
        val login = "unknownUser"

        every { userRepository.findByLogin(login) } returns null

        val exception = assertThrows<NoSuchElementException> {
            userService.getByLogin(login)
        }

        assertEquals("User with login $login not found", exception.message)
        verify { userRepository.findByLogin(login) }
    }
}
