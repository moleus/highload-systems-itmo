package itmo.highload.domain.entity

import itmo.highload.api.dto.AdoptionStatus
import java.time.LocalDateTime

data class AdoptionRequestEntity(
    val id: Int = 0,
    val dateTime: LocalDateTime,
    var status: AdoptionStatus,
    val customerId: Int,
    var managerId: Int? = null,
    val animalId: Int
)
