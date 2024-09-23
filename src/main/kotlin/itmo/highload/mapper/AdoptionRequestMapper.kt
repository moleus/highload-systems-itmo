package itmo.highload.mapper

import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.Animal
import itmo.highload.model.Customer
import itmo.highload.model.enum.AdoptionStatus
import java.time.LocalDateTime

object AdoptionRequestMapper {
    fun toEntity(customer: Customer, animal: Animal, status: AdoptionStatus): AdoptionRequest {
        return AdoptionRequest(
            dateTime = LocalDateTime.now(),
            status = status,
            customer = customer,
            manager = null,
            animal = animal
        )
    }

    fun toResponse(entity: AdoptionRequest): AdoptionRequestResponse {
        return AdoptionRequestResponse(
            id = entity.id,
            dateTime = entity.dateTime,
            status = entity.status,
            customer = UserMapper.toResponse(entity.customer),
            manager = UserMapper.toResponse(entity.manager),
            animal = AnimalMapper.toAnimalResponse(entity.animal)
        )
    }
}
