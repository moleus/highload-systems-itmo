package itmo.highload.api.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateAdoptionRequestStatusDto(

    @field:NotNull(message = "must not be null")
    val id: Int?,

    @field:NotNull(message = "must not be null")
    val status: AdoptionStatus?
)
