package itmo.highload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.model.enum.AdoptionStatus
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateAdoptionRequestStatusDto(

    @field:NotNull
    val id: Int?,

    @field:NotNull
    val status: AdoptionStatus?
)
