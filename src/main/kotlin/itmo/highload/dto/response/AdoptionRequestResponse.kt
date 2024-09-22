package itmo.highload.dto.response

import java.time.LocalDateTime

class AdoptionRequestResponse(
    val id: Int,
    val dateTime: LocalDateTime,
//    val status: AdoptionRequestStatus,
    val customer: UserResponse,
    val manager: UserResponse,
    val animal: AnimalResponse
)
