package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.domain.interactor.AnimalService
import itmo.highload.infrastructure.http.AnimalController
import org.apache.kafka.common.errors.InvalidRequestException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AnimalControllerTest {

    private val animalService = mockk<AnimalService>()
    private val controller = AnimalController(animalService)

    @Test
    fun `getAll - should return all animals`() {
        val token = "validToken"
        val animals = listOf(
            AnimalEntity(
                id = 1, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
                healthStatus = HealthStatus.HEALTHY
            ),
            AnimalEntity(
                id = 2, name = "Kate", typeOfAnimal = "Dog", gender = Gender.MALE, isCastrated = false,
                healthStatus = HealthStatus.HEALTHY
            )
        )

        every { animalService.getAll(null, null, token) } returns Flux.fromIterable(
            animals
        )

        StepVerifier.create(controller.getAll(null, null, token))
            .expectNextMatches {
                it.id == 1 && it.name == "Max" && it.type == "Cat" && it.gender == Gender.MALE &&
                        !it.isCastrated && it.healthStatus == HealthStatus.HEALTHY
            }
            .expectNextMatches {
                it.id == 2 && it.name == "Kate" && it.type == "Dog" && it.gender == Gender.MALE &&
                        !it.isCastrated && it.healthStatus == HealthStatus.HEALTHY
            }
            .verifyComplete()
    }

    @Test
    fun `getAnimal - should return animal by ID`() {
        val animalId = 1
        val animal = AnimalEntity(
            id = 1, name = "Max", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalService.getById(animalId) } returns Mono.just(animal)

        StepVerifier.create(controller.getAnimal(animalId))
            .expectNextMatches {
                it.id == animalId && it.name == "Max" && it.type == "Cat" &&
                        it.gender == Gender.MALE && !it.isCastrated && it.healthStatus == HealthStatus.HEALTHY
            }
            .verifyComplete()
    }

    @Test
    fun `addAnimal - should add a new animal`() {
        val request = AnimalDto(
            name = "Ave", type = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val animal = AnimalEntity(
            id = 1, name = "Ave", typeOfAnimal = "Cat", gender = Gender.MALE, isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalService.save(request) } returns Mono.just(animal)

        StepVerifier.create(controller.addAnimal(request))
            .expectNextMatches {
                it.id == 1 && it.name == "Ave" && it.type == "Cat" && it.gender == Gender.MALE &&
                        !it.isCastrated && it.healthStatus == HealthStatus.HEALTHY
            }
            .verifyComplete()
    }

    @Test
    fun `deleteAnimal - should delete an animal`() {
        val animalId = 1
        val token = "validToken"

        every { animalService.delete(animalId, token) } returns Mono.empty()

        StepVerifier.create(controller.deleteAnimal(animalId, token))
            .verifyComplete()
    }

    @Test
    fun `updateAnimal - should update animal information`() {
        val animalId = 1
        val request = AnimalDto(
            name = "Bella", type = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )
        val updatedAnimal = AnimalEntity(
            id = animalId, name = "Bella", typeOfAnimal = "Dog", gender = Gender.FEMALE,
            isCastrated = true, healthStatus = HealthStatus.HEALTHY
        )

        every { animalService.update(animalId, request) } returns Mono.just(updatedAnimal)

        StepVerifier.create(controller.updateAnimal(animalId, request))
            .expectNextMatches {
                it.id == animalId && it.name == "Bella" && it.type == "Dog" &&
                        it.gender == Gender.FEMALE && it.isCastrated && it.healthStatus == HealthStatus.HEALTHY
            }
            .verifyComplete()
    }


    @Test
    fun `updateAnimal - should return 400 if invalid data provided`() {
        val animalId = 1
        val request = AnimalDto(
            name = "", type = "Dog", gender = Gender.FEMALE, isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        every { animalService.update(animalId, request) } returns Mono.error(
            InvalidRequestException("Invalid data provided")
        )

        StepVerifier.create(controller.updateAnimal(animalId, request))
            .expectError(InvalidRequestException::class.java)
            .verify()
    }

}
