package itmo.highload.model

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import java.time.LocalDateTime

object TransactionMapper {

    fun toEntity(dto: TransactionDto, userId: Int, balance: BalanceResponse, isDonation: Boolean): Transaction {
        return Transaction(
            dateTime = LocalDateTime.now(),
            userId = userId,
            balanceId = balance.id,
            moneyAmount = dto.moneyAmount!!,
            isDonation = isDonation
        )
    }

    fun toResponse(entity: Transaction, balance: BalanceResponse): TransactionResponse {
        return TransactionResponse(
            dateTime = entity.dateTime,
            purpose = PurposeResponse(
                id = balance.id,
                name = balance.purpose.name
            ),
            userId = entity.userId,
            moneyAmount = entity.moneyAmount,
            isDonation = entity.isDonation
        )
    }
}
