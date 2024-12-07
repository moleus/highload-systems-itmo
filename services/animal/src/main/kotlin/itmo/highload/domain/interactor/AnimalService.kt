package itmo.highload.domain.interactor

import com.hazelcast.core.HazelcastInstance
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.HealthStatus
import itmo.highload.domain.AnimalRepository
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.domain.mapper.AnimalMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimalService(
    private val animalRepository: AnimalRepository,
    private val adoptionService: AdoptionService,
    private val animalImageService: AnimalImageService,
    @Qualifier("hazelcastInstance")
    private val hazelcastInstance: HazelcastInstance
) {

    private val cacheName = "animals"

    private fun initializeCacheIfEmpty(): Mono<Void> {
        val cache = hazelcastInstance.getMap<Int, AnimalEntity>(cacheName)
        if (cache.isEmpty) {
            return animalRepository.findAll()
                .doOnNext { animal ->
                    cache[animal.id] = AnimalMapper.toEntity(animal)
                }
                .then()
        }
        return Mono.empty()
    }

    fun getById(animalId: Int): Mono<AnimalEntity> {
        val cache = hazelcastInstance.getMap<Int, AnimalEntity>(cacheName)

        return initializeCacheIfEmpty().then(
            Mono.justOrEmpty(cache[animalId])
                .switchIfEmpty(
                    animalRepository.findById(animalId)
                        .switchIfEmpty(Mono.error(EntityNotFoundException("Animal with ID $animalId not found")))
                        .map { animal ->
                            val entity = AnimalMapper.toEntity(animal)
                            cache[animalId] = entity
                            entity
                        }
                )
        )
    }

    fun save(request: AnimalDto): Mono<AnimalEntity> {
        val cache = hazelcastInstance.getMap<Int, AnimalEntity>(cacheName)
        return initializeCacheIfEmpty().then(
            animalRepository.save(AnimalMapper.toJpaEntity(request))
                .map { animal ->
                    val entity = AnimalMapper.toEntity(animal)
                    cache[animal.id] = entity
                    entity
                }
        )
    }

    fun update(animalId: Int, request: AnimalDto): Mono<AnimalEntity> {
        val cache = hazelcastInstance.getMap<Int, AnimalEntity>(cacheName)

        return initializeCacheIfEmpty().then(
            getById(animalId).flatMap { existingAnimal ->
                validateAnimal(existingAnimal, request)
                existingAnimal.name = request.name
                existingAnimal.isCastrated = request.isCastrated!!
                existingAnimal.healthStatus = request.healthStatus!!

                animalRepository.save(AnimalMapper.toJpaEntity(existingAnimal))
                    .map { animal ->
                        val entity = AnimalMapper.toEntity(animal)
                        cache[animal.id] = entity
                        entity
                    }
            }
        )
    }

    fun delete(animalId: Int, token: String): Mono<Void> {
        val cache = hazelcastInstance.getMap<Int, AnimalEntity>(cacheName)

        return initializeCacheIfEmpty().then(
            getById(animalId).flatMap { existingAnimal ->
                animalRepository.delete(AnimalMapper.toJpaEntity(existingAnimal))
                    .then(animalImageService.deleteAllByAnimalId(existingAnimal.id, token))
                    .doOnSuccess { cache.remove(animalId) }
            }
        )
    }

    fun getAll(name: String?, isNotAdopted: Boolean?, token: String): Flux<AnimalEntity> {
        return initializeCacheIfEmpty().thenMany(
            when {
                isNotAdopted == true -> adoptionService.getAllAdoptedAnimalsId(token).collectList()
                else -> Mono.just(emptyList())
            }.flatMapMany { adoptedAnimalsId ->
                when {
                    isNotAdopted == true || isNotAdopted == null -> {
                        if (name != null) animalRepository.findByNameAndIdNotIn(name, adoptedAnimalsId)
                        else animalRepository.findByIdNotIn(adoptedAnimalsId)
                    }
                    else -> Flux.empty()
                }.map { animal -> AnimalMapper.toEntity(animal) }
            }
        )
    }

    private fun validateAnimal(existingAnimal: AnimalEntity, updateAnimal: AnimalDto) {
        val errors = mutableListOf<String>()

        if (existingAnimal.healthStatus == HealthStatus.DEAD) {
            errors.add("Can't update dead animal")
        }
        if (existingAnimal.gender != updateAnimal.gender) {
            errors.add("Can't change gender")
        }
        if (existingAnimal.typeOfAnimal != updateAnimal.type) {
            errors.add("Can't change type of animal")
        }
        if (existingAnimal.isCastrated && !updateAnimal.isCastrated!!) {
            errors.add("Can't cancel castration of an animal")
        }

        if (errors.isNotEmpty()) {
            throw InvalidAnimalUpdateException(errors.joinToString("; "))
        }
    }
}
