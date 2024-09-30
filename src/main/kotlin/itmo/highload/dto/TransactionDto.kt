package itmo.highload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransactionDto(

    @field:NotNull
    val purposeId: Int?,

    @field:NotNull
    @field:Min(1)
    val moneyAmount: Int?
)
