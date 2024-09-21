package itmo.highload.dto

import jakarta.validation.constraints.NotNull

data class UpdateAdoptionRequestStatusDto(
    @NotNull
    val id: Int,
//    @NotNull
//    val status: AdoptionRequestStatus
)
