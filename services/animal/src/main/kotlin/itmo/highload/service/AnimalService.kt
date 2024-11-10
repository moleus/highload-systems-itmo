package itmo.highload.service

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.HealthStatus
import itmo.highload.exceptions.InvalidAnimalUpdateException
import itmo.highload.model.Animal
import itmo.highload.model.AnimalMapper
import itmo.highload.repository.AnimalRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimalService(
    private val animalRepository: AnimalRepository,
    private val adoptionService: AdoptionService,
    private val animalImageService: AnimalImageService
) {

    fun getById(animalId: Int): Mono<Animal> = animalRepository.findById(animalId)
        .switchIfEmpty(Mono.error(EntityNotFoundException("Animal with ID $animalId not found")))

    fun save(request: AnimalDto): Mono<Animal> = animalRepository.save(AnimalMapper.toEntity(request))

    fun update(animalId: Int, request: AnimalDto): Mono<Animal> {
        return getById(animalId).flatMap { existingAnimal ->
            validateAnimal(existingAnimal, request)
            existingAnimal.name = request.name
            existingAnimal.isCastrated = request.isCastrated!!
            existingAnimal.healthStatus = request.healthStatus!!
            animalRepository.save(existingAnimal)
        }
    }

    fun delete(animalId: Int, token: String): Mono<Void> = getById(animalId).flatMap { existingAnimal ->
        animalRepository.delete(existingAnimal)
            .then(animalImageService.deleteAllByAnimalId(existingAnimal.id, token))
    }

    fun getAll(name: String?, isNotAdopted: Boolean?, token: String): Flux<Animal> {
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
                }
            }

    }

    private fun validateAnimal(existingAnimal: Animal, updateAnimal: AnimalDto) {
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
