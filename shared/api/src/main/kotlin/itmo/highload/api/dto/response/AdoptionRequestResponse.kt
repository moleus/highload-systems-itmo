package itmo.highload.api.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.api.dto.AdoptionStatus
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AdoptionRequestResponse(
    val id: Int,
    val dateTime: LocalDateTime,
    val status: AdoptionStatus,
    val customerId: Int,
    val managerId: Int?,
    val animalId: Int
)
