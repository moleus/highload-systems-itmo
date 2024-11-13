package itmo.highload.domain.mapper

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.domain.entity.AdoptionRequestEntity
import itmo.highload.infrastructure.postgres.model.AdoptionRequest
import java.time.LocalDateTime

object AdoptionRequestMapper {
    fun toEntity(request: AdoptionRequest): AdoptionRequestEntity {
        return AdoptionRequestEntity(
            id = request.id,
            dateTime = request.dateTime,
            status = request.status,
            customerId = request.customerId,
            managerId = request.managerId,
            animalId = request.animalId
        )
    }

    fun toJpaEntity(customerId: Int, animalId: Int, status: AdoptionStatus): AdoptionRequest {
        return AdoptionRequest(
            dateTime = LocalDateTime.now(),
            status = status,
            customerId = customerId,
            managerId = null,
            animalId = animalId
        )
    }

    fun toResponse(entity: AdoptionRequestEntity): AdoptionRequestResponse {
        return AdoptionRequestResponse(
            id = entity.id,
            dateTime = entity.dateTime,
            status = entity.status,
            customerId = entity.customerId,
            managerId = entity.managerId,
            animalId = entity.animalId
        )
    }

    fun toResponse(request: AdoptionRequest): AdoptionRequestResponse {
        return AdoptionRequestResponse(
            id = request.id,
            dateTime = request.dateTime,
            status = request.status,
            customerId = request.customerId,
            managerId = request.managerId,
            animalId = request.animalId
        )
    }
}
