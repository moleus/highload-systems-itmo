package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.model.Animal
import itmo.highload.repository.AnimalRepository
import itmo.highload.service.AdoptionService
import itmo.highload.service.AnimalService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class AnimalServiceTest {

    private val animalRepository: AnimalRepository = mockk()
    private val adoptionService: AdoptionService = mockk()
    private val animalService = AnimalService(animalRepository, adoptionService)

    private val existingAnimal = Animal(
        id = 1,
        name = "Buddy",
        typeOfAnimal = "Dog",
        gender = Gender.MALE,
        isCastrated = false,
        healthStatus = HealthStatus.HEALTHY
    )

    @Test
    fun `should update animal when valid`() {
        val request = AnimalDto(
            name = "Bobik",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.RECOVERING
        )

        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)
        every { animalRepository.save(existingAnimal) } returns Mono.just(existingAnimal.copy(isCastrated = false))

        animalService.update(1, request).subscribe { updatedAnimal ->
            assertEquals("Buddy", updatedAnimal.name)
            assertEquals("Dog", updatedAnimal.typeOfAnimal)
            assertEquals(true, updatedAnimal.isCastrated)
            assertFalse(updatedAnimal.isCastrated)
            verify { animalRepository.save(existingAnimal) }
        }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when updating dead animal`() {
        existingAnimal.healthStatus = HealthStatus.DEAD

        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't update dead animal"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when changing gender`() {
        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.FEMALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't change gender"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when changing type of animal`() {
        val request = AnimalDto(
            name = "Buddy",
            type = "Cat",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't change type of animal"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when cancelling castration`() {
        existingAnimal.isCastrated = true

        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't cancel castration of an animal"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }
}