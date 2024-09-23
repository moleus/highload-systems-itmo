@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.AnimalDto
import itmo.highload.model.Animal
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AnimalRepository
import itmo.highload.service.exception.InvalidAnimalUpdateException
import itmo.highload.service.mapper.AnimalMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AnimalService(private val animalRepository: AnimalRepository) {
    fun get(animalId: Int): Animal {
        return animalRepository.findById(animalId).orElseThrow {
            EntityNotFoundException("Animal with ID $animalId not found")
        }
    }

    fun save(request: AnimalDto): Animal {
        val animalEntity = AnimalMapper.toEntity(request)
        return animalRepository.save(animalEntity)
    }

    fun update(animalId: Int, request: AnimalDto): Animal {
        val existingAnimal = get(animalId)
        validateAnimal(existingAnimal, request)

        existingAnimal.name = request.name
        existingAnimal.isCastrated = request.isCastrated
        existingAnimal.healthStatus = request.healthStatus

        return animalRepository.save(existingAnimal)
    }

    fun delete(animalId: Int) {
        val existingAnimal = get(animalId)
        animalRepository.delete(existingAnimal)
    }

    fun getAll(pageable: Pageable): Page<Animal> {
        return animalRepository.findAll(pageable)
    }

    fun getAllByType(typeOfAnimal: String, pageable: Pageable): Page<Animal> {
        return animalRepository.findByTypeOfAnimal(typeOfAnimal, pageable)
    }

    fun getAllByName(name: String, pageable: Pageable): Page<Animal> {
        return animalRepository.findByName(name, pageable)
    }

    fun getAllByHealthStatus(healthStatus: HealthStatus, pageable: Pageable): Page<Animal> {
        return animalRepository.findByHealthStatus(healthStatus, pageable)
    }

    fun getAllByGender(gender: Gender, pageable: Pageable): Page<Animal> {
        return animalRepository.findByGender(gender, pageable)
    }

    fun getAllHealthStatus(): List<HealthStatus> {
        return animalRepository.findAllUniqueHealthStatuses()
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
        if (existingAnimal.isCastrated && !updateAnimal.isCastrated) {
            errors.add("Can't cancel castration of an animal")
        }

        if (errors.isNotEmpty()) {
            throw InvalidAnimalUpdateException(errors.joinToString("; "))
        }
    }
}
