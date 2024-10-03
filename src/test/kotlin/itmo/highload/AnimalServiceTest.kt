package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.dto.AnimalDto
import itmo.highload.model.Animal
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AnimalRepository
import itmo.highload.service.AnimalService
import itmo.highload.service.exception.InvalidAnimalUpdateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class AnimalServiceTest {

    private val animalRepository: AnimalRepository = mockk()
    private val animalService = AnimalService(animalRepository)

    @Test
    fun `should update animal when valid`() {
        val existingAnimal = Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val request = AnimalDto(
            name = "Bobik",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.RECOVERING
        )

        every { animalRepository.findById(1) } returns Optional.of(existingAnimal)
        every { animalRepository.save(existingAnimal) } returns existingAnimal.copy(isCastrated = false)

        val updatedAnimal = animalService.update(1, request)

        assertEquals("Buddy", updatedAnimal.name)
        assertFalse(updatedAnimal.isCastrated)
        verify { animalRepository.save(existingAnimal) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when updating dead animal`() {
        val existingAnimal = Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.DEAD
        )
        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Optional.of(existingAnimal)

        val exception = assertThrows<InvalidAnimalUpdateException> {
            animalService.update(1, request)
        }

        assertEquals("Can't update dead animal", exception.message)
        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when changing gender`() {
        val existingAnimal = Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )
        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.FEMALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Optional.of(existingAnimal)

        val exception = assertThrows<InvalidAnimalUpdateException> {
            animalService.update(1, request)
        }

        assertEquals("Can't change gender", exception.message)
        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when changing type of animal`() {
        val existingAnimal = Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )
        val request = AnimalDto(
            name = "Buddy",
            type = "Cat",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Optional.of(existingAnimal)

        val exception = assertThrows<InvalidAnimalUpdateException> {
            animalService.update(1, request)
        }

        assertEquals("Can't change type of animal", exception.message)
        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when cancelling castration`() {
        val existingAnimal = Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )
        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Optional.of(existingAnimal)

        val exception = assertThrows<InvalidAnimalUpdateException> {
            animalService.update(1, request)
        }

        assertEquals("Can't cancel castration of an animal", exception.message)
        verify(exactly = 0) { animalRepository.save(any()) }
    }
}
