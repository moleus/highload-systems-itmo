package itmo.highload.model

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.api.dto.response.UserResponse
import java.time.LocalDateTime

object TransactionMapper {

    fun toEntity(dto: TransactionDto, userId: Int, balance: Balance, isDonation: Boolean): Transaction {
        return Transaction(
            dateTime = LocalDateTime.now(),
            userId = userId,
            balance = balance,
            moneyAmount = dto.moneyAmount!!,
            isDonation = isDonation
        )
    }

    fun toResponse(entity: Transaction): TransactionResponse {
        return TransactionResponse(
            dateTime = entity.dateTime,
            purpose = PurposeResponse(
                id = entity.balance.id,
                name = entity.balance.purpose
            ),
            user = UserResponse(
                id = entity.userId,
//                login = entity.user.login
            ),
            moneyAmount = entity.moneyAmount,
            isDonation = entity.isDonation
        )
    }
}
