package itmo.highload.domain.mapper

import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.domain.entity.BalanceEntity
import itmo.highload.infrastructure.postgres.model.Balance

object BalanceMapper {

    fun toEntity(entity: Balance): BalanceEntity {
        return BalanceEntity(
            id = entity.id,
            purpose = entity.purpose,
            moneyAmount = entity.moneyAmount
        )
    }

    fun toJpaEntity(purposeName: String): Balance {
        return Balance(
            purpose = purposeName,
            moneyAmount = 0
        )
    }

    fun toBalanceResponse(entity: BalanceEntity): BalanceResponse {
        return BalanceResponse(
            id = entity.id,
            purpose = toPurposeResponse(entity),
            moneyAmount = entity.moneyAmount
        )
    }

    fun toPurposeResponse(entity: BalanceEntity): PurposeResponse {
        return PurposeResponse(
            id = entity.id,
            name = entity.purpose
        )
    }
}
