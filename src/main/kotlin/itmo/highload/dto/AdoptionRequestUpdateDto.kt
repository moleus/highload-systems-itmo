package itmo.highload.dto

import jakarta.validation.constraints.NotNull

data class AdoptionRequestUpdateDto(
    @NotNull
    val id: Int,
//    @NotNull
//    val status: AdoptionRequestStatus
)