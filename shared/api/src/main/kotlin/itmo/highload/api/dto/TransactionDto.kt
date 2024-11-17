package itmo.highload.api.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransactionDto(

    @field:NotNull(message = "must not be null")
    val purposeId: Int?,

    @field:NotNull(message = "must not be null")
    @field:Min(1, message = "must be greater than or equal to 1")
    val moneyAmount: Int?
)
