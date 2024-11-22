package itmo.highload.domain.entity

import java.time.LocalDateTime

data class TransactionEntity(
    val id: Int = 0,
    val dateTime: LocalDateTime,
    val userId: Int,
    val balanceId: Int,
    val moneyAmount: Int,
    val isDonation: Boolean,
    val status: String
)
