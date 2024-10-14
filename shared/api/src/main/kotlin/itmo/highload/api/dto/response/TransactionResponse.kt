package itmo.highload.api.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransactionResponse(
    val dateTime: LocalDateTime,
    val purpose: PurposeResponse,
    val user: UserResponse,
    val moneyAmount: Int,
    val isDonation: Boolean
)
