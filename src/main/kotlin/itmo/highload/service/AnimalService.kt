@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.model.Animal
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AnimalRepository
import itmo.highload.service.mapper.AnimalMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AnimalService (private val animalRepository: AnimalRepository) {
    fun get(animalId: Int): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalRepository.findById(animalId).get())
    }

    fun save(request: AnimalDto): AnimalResponse {
        val animalEntity = AnimalMapper.toEntity(request)
        val savedAnimal = animalRepository.save(animalEntity)
        return AnimalMapper.toAnimalResponse(savedAnimal)
    }

    fun update(animalId: Int, request: AnimalDto): AnimalResponse {
        val existingAnimal = animalRepository.findById(animalId).orElseThrow()
        validateAnimal(existingAnimal, request)

        existingAnimal.name = request.name
        existingAnimal.isCastrated = request.isCastrated
        existingAnimal.healthStatus = request.healthStatus

        val savedAnimal = animalRepository.save(existingAnimal)
        return AnimalMapper.toAnimalResponse(savedAnimal)

    }

    fun delete(animalId: Int) {
        val existingAnimal = animalRepository.findById(animalId).orElseThrow()
        animalRepository.delete(existingAnimal)
    }

    fun getAll(): List<AnimalResponse> {
        return animalRepository.findAll().map { AnimalMapper.toAnimalResponse(it) }
    }

    fun getAllByType(typeOfAnimal: String, pageable: Pageable): Page<AnimalResponse> {
        return animalRepository.findByTypeOfAnimal(typeOfAnimal, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    fun getAllByName(name: String, pageable: Pageable): Page<AnimalResponse> {
        return animalRepository.findByName(name, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    fun getAllByHealthStatus(healthStatus: HealthStatus, pageable: Pageable): Page<AnimalResponse> {
        return animalRepository.findByHealthStatus(healthStatus, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    fun getAllByGender(gender: Gender, pageable: Pageable): Page<AnimalResponse> {
        return animalRepository.findByGender(gender, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    private fun validateAnimal(existingAnimal: Animal, updateAnimal: AnimalDto) {
        require(existingAnimal.healthStatus != HealthStatus.DEAD) {"Can't update dead animal"}
        require(existingAnimal.gender == updateAnimal.gender) {"Can't change gender"}
        require(existingAnimal.typeOfAnimal == updateAnimal.type) {"Can't change type of animal"}
        require(!(existingAnimal.isCastrated && !updateAnimal.isCastrated)) {"Can't cancel castration of an animal"}
    }
}

