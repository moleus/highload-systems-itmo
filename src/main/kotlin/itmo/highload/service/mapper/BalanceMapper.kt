package itmo.highload.service.mapper

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.model.Balance

object BalanceMapper {

    fun toEntity(purposeName: String): Balance {
        return Balance(
            purpose = purposeName,
            moneyAmount = 0
        )
    }

    fun toBalanceResponse(entity: Balance): BalanceResponse {
        return BalanceResponse(
            purpose = toPurposeResponse(entity),
            moneyAmount = entity.moneyAmount
        )
    }

    fun toPurposeResponse(entity: Balance): PurposeResponse {
        return PurposeResponse(
            id = entity.id,
            name = entity.purpose
        )
    }
}
