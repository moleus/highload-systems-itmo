package itmo.highload.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.model.enum.AdoptionStatus
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AdoptionRequestResponse(
    val id: Int,
    val dateTime: LocalDateTime,
    val status: AdoptionStatus,
    val customer: CustomerResponse,
    val manager: UserResponse?,
    val animal: AnimalResponse
)
