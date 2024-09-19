package itmo.highload.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class AnimalUpdateDto(
    @NotNull
    val id: Int,

    @Size(min = 1, max = 50)
    val name: String,

    @Size(min = 1, max = 50)
    val type: String,

//    val gender: Gender,

    val isCastrated: Boolean,

//    val healthStatus: HealthStatus
)