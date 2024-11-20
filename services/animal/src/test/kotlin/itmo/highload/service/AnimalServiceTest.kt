package itmo.highload.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.model.Animal
import itmo.highload.repository.AnimalRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import reactor.test.StepVerifier
import kotlin.test.assertTrue

class AnimalServiceTest {

    private val animalRepository: AnimalRepository = mockk()
    private val adoptionService: AdoptionService = mockk()
    private val animalImageService: AnimalImageService = mockk()
    private val animalService = AnimalService(animalRepository, adoptionService, animalImageService)

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
        every { animalRepository.save(existingAnimal) } returns Mono.just(existingAnimal.copy(
            name = "Bobik",
            isCastrated = true,
            healthStatus = HealthStatus.RECOVERING
        ))

        animalService.update(1, request).subscribe { updatedAnimal ->
            assertEquals("Bobik", updatedAnimal.name)
            assertEquals("Dog", updatedAnimal.typeOfAnimal)
            assertTrue(updatedAnimal.isCastrated)
            assertEquals(HealthStatus.RECOVERING, updatedAnimal.healthStatus)
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


    @Test
    fun `save - should save a new animal`() {
        val request = AnimalDto(name = "Ave", type = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY)
        val savedAnimal = Animal(id = 1, name = "Ave", typeOfAnimal = "Cat", gender = Gender.MALE,
            isCastrated = false, healthStatus = HealthStatus.HEALTHY)

        every { animalRepository.save(any()) } returns Mono.just(savedAnimal)

        StepVerifier.create(animalService.save(request))
            .expectNext(savedAnimal)
            .verifyComplete()

        verify { animalRepository.save(any()) }
    }

    @Test
    fun `getAll - should return all animals not adopted`() {
        val token = "validToken"
        val animal1 = Animal(id = 1, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY)
        val animal2 = Animal(id = 2, name = "Bella", typeOfAnimal = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY)

        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.empty()
        every { animalRepository.findByIdNotIn(any()) } returns Flux.just(animal1, animal2)

        StepVerifier.create(animalService.getAll(null, true, token))
            .expectNext(animal1)
            .expectNext(animal2)
            .verifyComplete()
    }

    @Test
    fun `getAll - should return filtered animals by name`() {
        val token = "validToken"
        val animal1 = Animal(id = 1, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY)
        val animal2 = Animal(id = 2, name = "Bella", typeOfAnimal = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY)

        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.empty()
        every { animalRepository.findByNameAndIdNotIn("Max", any()) } returns Flux.just(animal1)

        StepVerifier.create(animalService.getAll("Max", true, token))
            .expectNext(animal1)
            .verifyComplete()
    }

    @Test
    fun `getAll - should return empty if no animals found`() {
        val token = "validToken"

        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.empty()
        every { animalRepository.findByIdNotIn(any()) } returns Flux.empty()

        StepVerifier.create(animalService.getAll(null, true, token))
            .verifyComplete()
    }

    @Test
    fun `getAll - should return animals filtered by adoption status`() {
        val token = "validToken"
        val animal1 = Animal(id = 1, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY)
        val animal2 = Animal(id = 2, name = "Bella", typeOfAnimal = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY)

        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.empty()
        every { animalRepository.findByIdNotIn(any()) } returns Flux.just(animal1, animal2)

        StepVerifier.create(animalService.getAll(null, true, token))
            .expectNext(animal1)
            .expectNext(animal2)
            .verifyComplete()

        verify { adoptionService.getAllAdoptedAnimalsId(token) }
        verify { animalRepository.findByIdNotIn(any()) }
    }

    @Test
    fun `getAll - should call adoptionService when isNotAdopted is true`() {
        val token = "validToken"
        val animal2 = Animal(id = 2, name = "Bella", typeOfAnimal = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY)

        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.just(1)
        every { animalRepository.findByIdNotIn(any()) } returns Flux.just(animal2)

        StepVerifier.create(animalService.getAll(null, true, token))
            .expectNext(animal2)
            .verifyComplete()

        verify { adoptionService.getAllAdoptedAnimalsId(token) }
        verify { animalRepository.findByIdNotIn(any()) }
    }

    @Test
    fun `delete - should delete animal and its images`() {
        val animalId = 1
        val token = "validToken"
        val animal = Animal(id = animalId, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false, healthStatus = HealthStatus.HEALTHY)

        every { animalRepository.findById(animalId) } returns Mono.just(animal)
        every { animalRepository.delete(animal) } returns Mono.empty()
        every { animalImageService.deleteAllByAnimalId(animal.id, token) } returns Mono.empty()

        StepVerifier.create(animalService.delete(animalId, token))
            .verifyComplete()

        verify { animalRepository.delete(animal) }
        verify { animalImageService.deleteAllByAnimalId(animal.id, token) }
    }

}
