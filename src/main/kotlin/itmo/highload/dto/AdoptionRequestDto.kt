package itmo.highload.dto

import jakarta.validation.constraints.NotNull

data class AdoptionRequestDto(
    @NotNull
    val id: Int,
    @NotNull
    val status: AdoptionRequestStatus
)
