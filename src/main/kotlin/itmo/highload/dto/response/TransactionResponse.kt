package itmo.highload.dto.response

import java.time.LocalDateTime

class TransactionResponse(
    val dateTime: LocalDateTime,
    val purpose: PurposeResponse,
    val user: UserResponse,
    val moneyAmount: Int,
    val isDonation: Boolean
)
