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
import reactor.test.StepVerifier
import kotlin.test.assertTrue

class AnimalServiceTest {

    private val animalRepository: AnimalRepository = mockk()
    private val adoptionService: AdoptionService = mockk()
    private val animalImageService: AnimalImageService = mockk()
    private val hazelcastInstance: HazelcastInstance = mockk()
    private val animalService = AnimalService(animalRepository, adoptionService, animalImageService, hazelcastInstance)

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
            type = "Dog",
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

        val hazelcastMap = mockk<IMap<Int, AnimalEntity>>(relaxed = true)
        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns hazelcastMap
        every { hazelcastMap[animalId] } returns null
        every { hazelcastMap.remove(animalId) } returns animalEntity
        every { animalRepository.findById(animalId) } returns Mono.just(mappedAnimal)
        every { animalRepository.delete(mappedAnimal) } returns Mono.empty()
        every { animalImageService.deleteAllByAnimalId(animalId, token) } returns Mono.empty()

        StepVerifier.create(animalService.delete(animalId, token))
            .verifyComplete()

        verify { animalRepository.delete(mappedAnimal) }
        verify { animalImageService.deleteAllByAnimalId(animalId, token) }
        verify { hazelcastMap.remove(animalId) }
    }

    @Test
    fun `should save animal and update cache`() {
        val request = AnimalDto(
            name = "Max",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val savedAnimal = Animal(
            id = 1,
            name = "Max",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val cache = mockk<IMap<Int, AnimalEntity>>(relaxed = true)

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns cache
        every { animalRepository.save(AnimalMapper.toJpaEntity(request)) } returns Mono.just(savedAnimal)

        StepVerifier.create(animalService.save(request))
            .assertNext { animalEntity ->
                assertEquals(savedAnimal.id, animalEntity.id)
                assertEquals(savedAnimal.name, animalEntity.name)
                assertEquals(savedAnimal.typeOfAnimal, animalEntity.typeOfAnimal)
            }
            .verifyComplete()

        verify(exactly = 1) { animalRepository.save(AnimalMapper.toJpaEntity(request)) }
        verify(exactly = 1) { cache[savedAnimal.id] = AnimalMapper.toEntity(savedAnimal) }
    }

    @Test
    fun `should get all animals excluding adopted ones when isNotAdopted is true`() {
        val token = "some-token"
        val adoptedAnimalsId = listOf(2, 3)
        val requestName = "Buddy"

        val animals = listOf(
            Animal(
                id = 1,
                name = "Buddy",
                typeOfAnimal = "Dog",
                gender = Gender.MALE,
                isCastrated = true,
                healthStatus = HealthStatus.HEALTHY
            ),
            Animal(
                id = 4,
                name = "Charlie",
                typeOfAnimal = "Dog",
                gender = Gender.MALE,
                isCastrated = true,
                healthStatus = HealthStatus.HEALTHY
            )
        )

        val cache = mockk<IMap<Int, AnimalEntity>>(relaxed = true)

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns cache
        every { adoptionService.getAllAdoptedAnimalsId(token) } returns Flux.just(2, 3)
        every { animalRepository.findByNameAndIdNotIn(requestName, adoptedAnimalsId) } returns Flux.just(animals[0])

        StepVerifier.create(animalService.getAll(requestName, true, token))
            .assertNext { animal ->
                assertEquals("Buddy", animal.name)
            }
            .verifyComplete()

        verify(exactly = 1) { adoptionService.getAllAdoptedAnimalsId(token) }
        verify(exactly = 1) { animalRepository.findByNameAndIdNotIn(requestName, adoptedAnimalsId) }
    }


    @Test
    fun `should update animal successfully`() {
        val animalId = 1
        val request = AnimalDto(
            name = "Updated Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )
        val existingAnimal = AnimalEntity(
            id = animalId,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = false,
            healthStatus = HealthStatus.HEALTHY
        )
        val updatedAnimal = Animal(
            id = animalId,
            name = "Updated Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val cache = mockk<IMap<Int, AnimalEntity>>(relaxed = true)

        every { hazelcastInstance.getMap<Int, AnimalEntity>("animals") } returns cache
        every { animalRepository.findById(animalId) } returns Mono.just(updatedAnimal)
        every { animalRepository.save(any()) } returns Mono.just(updatedAnimal)

        every { cache[animalId] } returns existingAnimal

        StepVerifier.create(animalService.update(animalId, request))
            .assertNext { animal ->
                assertEquals("Updated Buddy", animal.name)
                assertTrue(animal.isCastrated)
                assertEquals(HealthStatus.HEALTHY, animal.healthStatus)
            }
            .verifyComplete()

        verify(exactly = 1) { animalRepository.save(any()) }
        verify(exactly = 1) { cache[animalId] = any() }
    }
}
