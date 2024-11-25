package itmo.highload.domain.interactor

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.HealthStatus
import itmo.highload.domain.AnimalRepository
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.domain.mapper.AnimalMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimalService(
    private val animalRepository: AnimalRepository,
    private val adoptionService: AdoptionService,
    private val animalImageService: AnimalImageService
) {

    @Cacheable(cacheNames = ["animals"], key = "#animalId")
    fun getById(animalId: Int): Mono<AnimalEntity> = animalRepository.findById(animalId)
        .switchIfEmpty(Mono.error(EntityNotFoundException("Animal with ID $animalId not found")))
        .map { animal -> AnimalMapper.toEntity(animal) }

    fun save(request: AnimalDto): Mono<AnimalEntity> =
        animalRepository.save(AnimalMapper.toJpaEntity(request)).map { animal -> AnimalMapper.toEntity(animal) }

    fun update(animalId: Int, request: AnimalDto): Mono<AnimalEntity> {
        return getById(animalId).flatMap { existingAnimal ->
            validateAnimal(existingAnimal, request)
            existingAnimal.name = request.name
            existingAnimal.isCastrated = request.isCastrated!!
            existingAnimal.healthStatus = request.healthStatus!!
            animalRepository.save(AnimalMapper.toJpaEntity(existingAnimal))
                .map { animal -> AnimalMapper.toEntity(animal) }
        }
    }

    fun delete(animalId: Int, token: String): Mono<Void> = getById(animalId).flatMap { existingAnimal ->
        animalRepository.delete(AnimalMapper.toJpaEntity(existingAnimal))
            .then(animalImageService.deleteAllByAnimalId(existingAnimal.id, token))
    }

    @Cacheable(cacheNames = ["animalList"], key = "#name ?: 'all'")
    fun getAll(name: String?, isNotAdopted: Boolean?, token: String): Flux<AnimalEntity> {
        val adoptedAnimalsIdFlux: Flux<Int> = if (isNotAdopted != null && isNotAdopted)
            adoptionService.getAllAdoptedAnimalsId(token) else Flux.empty()

        return adoptedAnimalsIdFlux.collectList()
            .flatMapMany { adoptedAnimalsId ->
                when {
                    isNotAdopted == true || isNotAdopted == null -> {
                        if (name != null) animalRepository.findByNameAndIdNotIn(name, adoptedAnimalsId)
                        else animalRepository.findByIdNotIn(adoptedAnimalsId)
                    }
                    else -> {
                        Flux.empty()
                    }
                }.map { animal -> AnimalMapper.toEntity(animal) }
            }

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
