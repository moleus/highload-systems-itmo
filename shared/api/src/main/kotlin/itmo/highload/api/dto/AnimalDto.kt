package itmo.highload.api.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AnimalDto(

    @field:Size(min = 1, max = 50)
    @field:NotBlank
    val name: String,

    @field:Size(min = 1, max = 50)
    @field:NotBlank
    val type: String,

    @field:NotNull
    val gender: Gender?,

    @field:NotNull
    val isCastrated: Boolean?,

    @field:NotNull
    val healthStatus: HealthStatus?
)
