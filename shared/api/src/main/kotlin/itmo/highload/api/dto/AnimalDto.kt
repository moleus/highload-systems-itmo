package itmo.highload.api.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AnimalDto(

    @field:Size(min = 1, max = 50, message = "size must be between 1 and 50")
    @field:NotBlank(message = "must not be empty")
    val name: String,

    @field:Size(min = 1, max = 50, message = "size must be between 1 and 50")
    @field:NotBlank(message = "must not be empty")
    val type: String,

    @field:NotNull(message = "must not be null")
    val gender: Gender?,

    @field:NotNull(message = "must not be null")
    val isCastrated: Boolean?,

    @field:NotNull(message = "must not be null")
    val healthStatus: HealthStatus?
)
