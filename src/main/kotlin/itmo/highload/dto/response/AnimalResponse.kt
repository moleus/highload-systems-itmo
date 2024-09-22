package itmo.highload.dto.response

import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus

class AnimalResponse(
    val id: Int,
    val name: String,
    val type: String,
    val gender: Gender,
    val isCastrated: Boolean,
    val healthStatus: HealthStatus
)
