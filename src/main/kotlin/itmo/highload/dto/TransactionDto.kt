package itmo.highload.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class TransactionDto(
    @NotNull
    val purposeId: Int,
    @NotNull
    @Min(0)
    val moneyAmount: Int
)
