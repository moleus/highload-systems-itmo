package itmo.highload.api.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AnimalResponse(
    val id: Int,
    val name: String,
    val type: String,
    val gender: Gender,
    val isCastrated: Boolean,
    val healthStatus: HealthStatus
)
