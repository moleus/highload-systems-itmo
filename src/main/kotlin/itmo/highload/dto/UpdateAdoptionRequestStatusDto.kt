package itmo.highload.dto

import itmo.highload.model.enum.AdoptionStatus
import jakarta.validation.constraints.NotNull

data class UpdateAdoptionRequestStatusDto(
    @NotNull
    val id: Int,
    @NotNull
    val status: AdoptionStatus
)
