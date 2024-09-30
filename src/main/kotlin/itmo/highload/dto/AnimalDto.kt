package itmo.highload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AnimalDto(

    @Size(min = 1, max = 50)
    @NotBlank
    val name: String,

    @Size(min = 1, max = 50)
    @NotBlank
    val type: String,

    @NotNull
    val gender: Gender,

    @NotNull
    val isCastrated: Boolean,

    @NotNull
    val healthStatus: HealthStatus
)
