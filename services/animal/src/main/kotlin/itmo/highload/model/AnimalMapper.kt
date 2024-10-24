package itmo.highload.model

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.response.AnimalResponse

object AnimalMapper {
    fun toEntity(animal: AnimalDto): Animal {
        return Animal(
            name = animal.name,
            typeOfAnimal = animal.type,
            gender = animal.gender!!,
            isCastrated = animal.isCastrated!!,
            healthStatus = animal.healthStatus!!
        )
    }

    fun toAnimalResponse(entity: Animal): AnimalResponse {
        return AnimalResponse(
            id = entity.id,
            name = entity.name,
            type = entity.typeOfAnimal,
            gender = entity.gender,
            isCastrated = entity.isCastrated,
            healthStatus = entity.healthStatus
        )
    }
}
