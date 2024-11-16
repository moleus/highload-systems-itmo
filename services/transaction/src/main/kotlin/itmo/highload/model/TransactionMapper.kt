package itmo.highload.model

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.kafka.TransactionBalanceMessage
import itmo.highload.kafka.TransactionResultMessage
import java.time.LocalDateTime

object TransactionMapper {

    fun toEntity(dto: TransactionDto, userId: Int, balance: BalanceResponse, isDonation: Boolean): Transaction {
        return Transaction(
            dateTime = LocalDateTime.now(),
            userId = userId,
            balanceId = balance.id,
            moneyAmount = dto.moneyAmount!!,
            isDonation = isDonation,
            status = "PENDING"
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
            isDonation = entity.isDonation,
            status = entity.status
        )
    }

    fun toBalanceMessage(entity: Transaction): TransactionBalanceMessage {
        return TransactionBalanceMessage(
            dateTime = entity.dateTime,
            transactionId = entity.id,
            balanceId = entity.balanceId,
            moneyAmount = entity.moneyAmount,
            isDonation = entity.isDonation
        )
    }

    fun toEntityFromTransactionDTO(dto: TransactionDto, userId: Int, isDonation: Boolean): Transaction {
        return Transaction(
            dateTime = LocalDateTime.now(),
            userId = userId,
            balanceId = dto.purposeId!!,
            moneyAmount = dto.moneyAmount!!,
            isDonation = isDonation,
            status = "PENDING"
        )
    }

    fun toResponseFromTransaction(entity: Transaction): TransactionResponse {
        return TransactionResponse(
            dateTime = entity.dateTime,
            purpose = PurposeResponse(
                id = entity.balanceId,
                name = null
            ),
            userId = entity.userId,
            moneyAmount = entity.moneyAmount,
            isDonation = entity.isDonation,
            status = entity.status
        )
    }

    fun toTransactionRollBackMessageFromResultMessage(resultMessage: TransactionResultMessage):
            TransactionBalanceMessage {
        return TransactionBalanceMessage(
            dateTime = resultMessage.dateTime,
            transactionId = resultMessage.transactionId,
            balanceId = resultMessage.balanceId,
            moneyAmount = resultMessage.moneyAmount,
            isDonation = false
        )
    }
}
