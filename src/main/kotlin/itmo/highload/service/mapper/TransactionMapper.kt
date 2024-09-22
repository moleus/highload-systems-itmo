package itmo.highload.service.mapper

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.dto.response.UserResponse
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.User
import java.time.LocalDateTime

object TransactionMapper {

    fun toEntity(dto: TransactionDto, user: User, balance: Balance, isDonation: Boolean): Transaction {
        return Transaction(
            dateTime = LocalDateTime.now(),
            user = user,
            balance = balance,
            moneyAmount = dto.moneyAmount,
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
                id = entity.user.id,
                login = entity.user.login
            ),
            moneyAmount = entity.moneyAmount,
            isDonation = entity.isDonation
        )
    }
}
