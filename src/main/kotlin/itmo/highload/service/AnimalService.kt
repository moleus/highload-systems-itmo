package itmo.highload.service

import itmo.highload.dto.AnimalDto
import itmo.highload.mapper.AnimalMapper
import itmo.highload.model.Animal
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AnimalRepository
import itmo.highload.service.exception.InvalidAnimalUpdateException
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AnimalService(private val animalRepository: AnimalRepository) {

    fun getById(animalId: Int): Animal {
        return animalRepository.findById(animalId).orElseThrow {
            EntityNotFoundException("Animal with ID $animalId not found")
        }
    }

    fun save(request: AnimalDto): Animal {
        val animalEntity = AnimalMapper.toEntity(request)
        return animalRepository.save(animalEntity)
    }

    fun update(animalId: Int, request: AnimalDto): Animal {
        val existingAnimal = getById(animalId)
        validateAnimal(existingAnimal, request)

        existingAnimal.name = request.name
        existingAnimal.isCastrated = request.isCastrated!!
        existingAnimal.healthStatus = request.healthStatus!!

        return animalRepository.save(existingAnimal)
    }

    fun delete(animalId: Int) {
        val existingAnimal = getById(animalId)
        animalRepository.delete(existingAnimal)
    }

    fun getAll(name: String?, pageable: Pageable): Page<Animal> {
        if (name != null) {
            return animalRepository.findByName(name, pageable)
        }
        return animalRepository.findAll(pageable)
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
