package itmo.highload.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DonationDto(
    @NotBlank
    val purposeName: String,
    @NotNull
    @Min(0)
    val moneyAmount: Int
)
