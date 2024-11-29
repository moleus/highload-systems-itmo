package itmo.highload.service

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.domain.AnimalRepository
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.domain.interactor.AdoptionService
import itmo.highload.domain.interactor.AnimalImageService
import itmo.highload.domain.interactor.AnimalService
import itmo.highload.domain.mapper.AnimalMapper
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.infrastructure.postgres.model.Animal
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
    private val hazelcastInstance: HazelcastInstance = mockk()
    private val animalService = AnimalService(animalRepository, adoptionService, animalImageService, hazelcastInstance)

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
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals")[1] } returns null
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
        val existingAnimal = AnimalEntity(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.DEAD
        )

        val savedAnimal = Animal(
            id = 1,
            name = "Ave",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        val request = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val hazelcastMap = mockk<IMap<Int, AnimalEntity>>(relaxed = true)

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns hazelcastMap
        every { hazelcastMap[1] } returns existingAnimal
        every { animalRepository.findById(1) } returns Mono.just(savedAnimal)

        StepVerifier.create(animalService.update(1, request))
            .expectErrorMatches {
                it is InvalidAnimalUpdateException && it.message == "Can't update dead animal"
            }
            .verify()

        verify(exactly = 0) { animalRepository.save(any()) }
        verify(exactly = 0) { hazelcastMap.set(any(), any()) }
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

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals")[1] } returns null
        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals").set(any(), any()) } returns Unit

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't change gender"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidAnimalUpdateException when changing type of animal`() {
        val existingAnimal = AnimalEntity(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val request = AnimalDto(
            name = "Buddy",
            type = "Dog", // Пытаемся изменить тип
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val savedAnimal = Animal(
            id = 1,
            name = "Ave",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        val hazelcastMap = mockk<IMap<Int, AnimalEntity>>(relaxed = true)

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns hazelcastMap
        every { hazelcastMap[1] } returns existingAnimal
        every { animalRepository.findById(1) } returns Mono.just(savedAnimal)

        StepVerifier.create(animalService.update(1, request))
            .expectErrorMatches {
                it is InvalidAnimalUpdateException && it.message == "Can't change type of animal"
            }
            .verify()

        verify(exactly = 0) { animalRepository.save(any()) }
        verify(exactly = 0) { hazelcastMap.set(any(), any()) }
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

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals")[1] } returns null
        every { animalRepository.findById(1) } returns Mono.just(existingAnimal)
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals").set(any(), any()) } returns Unit

        animalService.update(1, request).test().verifyErrorMatches {
            it is InvalidAnimalUpdateException && it.message == "Can't cancel castration of an animal"
        }

        verify(exactly = 0) { animalRepository.save(any()) }
    }

    @Test
    fun `save - should save a new animal`() {
        val request = AnimalDto(
            name = "Ave",
            type = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val savedAnimalEntity = AnimalEntity(
            id = 1,
            name = "Ave",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val savedAnimal = Animal(
            id = 1,
            name = "Ave",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals")[1] } returns null
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals").set(any(), any()) } returns Unit
        every { animalRepository.save(any()) } returns Mono.just(savedAnimal)

        StepVerifier.create(animalService.save(request))
            .expectNext(savedAnimalEntity)
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
            .expectNext(AnimalMapper.toEntity(animal1))
            .expectNext(AnimalMapper.toEntity(animal2))
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
            .expectNext(AnimalMapper.toEntity(animal1))
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
            .expectNext(AnimalMapper.toEntity(animal1))
            .expectNext(AnimalMapper.toEntity(animal2))
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
            .expectNext(AnimalMapper.toEntity(animal2))
            .verifyComplete()

        verify { adoptionService.getAllAdoptedAnimalsId(token) }
        verify { animalRepository.findByIdNotIn(any()) }
    }

    @Test
    fun `delete - should delete animal and its images`() {
        val animalId = 1
        val token = "validToken"
        val animalEntity = AnimalEntity(
            id = animalId,
            name = "Max",
            typeOfAnimal = "Cat",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val mappedAnimal = Animal(
            id = animalEntity.id,
            name = animalEntity.name,
            typeOfAnimal = animalEntity.typeOfAnimal,
            gender = animalEntity.gender,
            isCastrated = animalEntity.isCastrated,
            healthStatus = animalEntity.healthStatus
        )

        // Мок операций
        val hazelcastMap = mockk<IMap<Int, AnimalEntity>>(relaxed = true)
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns hazelcastMap
        every { hazelcastMap[animalId] } returns null
        every { hazelcastMap.remove(animalId) } returns animalEntity
        every { animalRepository.findById(animalId) } returns Mono.just(mappedAnimal)
        every { animalRepository.delete(mappedAnimal) } returns Mono.empty()
        every { animalImageService.deleteAllByAnimalId(animalId, token) } returns Mono.empty()

        // Проверка
        StepVerifier.create(animalService.delete(animalId, token))
            .verifyComplete()

        verify { animalRepository.delete(mappedAnimal) }
        verify { animalImageService.deleteAllByAnimalId(animalId, token) }
        verify { hazelcastMap.remove(animalId) }
    }


}
