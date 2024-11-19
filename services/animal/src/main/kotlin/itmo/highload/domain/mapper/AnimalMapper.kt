package itmo.highload.domain.mapper

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.infrastructure.postgres.model.Animal

object AnimalMapper {
    fun toEntity(animal: Animal): AnimalEntity {
        return AnimalEntity(
            name = animal.name,
            typeOfAnimal = animal.typeOfAnimal,
            gender = animal.gender,
            isCastrated = animal.isCastrated,
            healthStatus = animal.healthStatus
        )
    }
    fun toJpaEntity(animal: AnimalDto): Animal {
        return Animal(
            name = animal.name,
            typeOfAnimal = animal.type,
            gender = animal.gender!!,
            isCastrated = animal.isCastrated!!,
            healthStatus = animal.healthStatus!!
        )
    }
    fun toJpaEntity(animal: AnimalEntity): Animal {
        return Animal(
            name = animal.name,
            typeOfAnimal = animal.typeOfAnimal,
            gender = animal.gender,
            isCastrated = animal.isCastrated,
            healthStatus = animal.healthStatus
        )
    }

    fun toAnimalResponse(entity: AnimalEntity): AnimalResponse {
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
