package itmo.highload.dto.response

import itmo.highload.model.enum.AdoptionStatus
import java.time.LocalDateTime

class AdoptionRequestResponse(
    val id: Int,
    val dateTime: LocalDateTime,
    val status: AdoptionStatus,
    val customer: UserResponse,
    val manager: UserResponse?,
    val animal: AnimalResponse
)
