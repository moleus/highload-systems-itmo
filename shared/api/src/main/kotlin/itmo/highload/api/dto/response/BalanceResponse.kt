package itmo.highload.api.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BalanceResponse(
    val id: Int,
    val purpose: PurposeResponse,
    val moneyAmount: Int
)
