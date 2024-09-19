package itmo.highload.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ExpenseDto(
    @NotNull
    val purposeName: Int,
    @NotNull
    @Min(0)
    val moneyAmount: Int
)