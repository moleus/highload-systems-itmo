package itmo.highload.model

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.api.dto.AdoptionStatus
import java.time.LocalDateTime

object AdoptionRequestMapper {
    fun toEntity(customerId: Int, animalId: Int, status: AdoptionStatus): AdoptionRequest {
        return AdoptionRequest(
            dateTime = LocalDateTime.now(),
            status = status,
            customerId = customerId,
            managerId = null,
            animalId = animalId
        )
    }

    fun toResponse(entity: AdoptionRequest): AdoptionRequestResponse {
        return AdoptionRequestResponse(
            id = entity.id,
            dateTime = entity.dateTime,
            status = entity.status,
            customerId = entity.customerId,
            managerId = entity.managerId,
            animalId = entity.animalId,
        )
    }
}
