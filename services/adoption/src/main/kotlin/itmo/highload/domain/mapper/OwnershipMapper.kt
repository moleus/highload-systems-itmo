package itmo.highload.domain.mapper

import itmo.highload.domain.entity.OwnershipEntity
import itmo.highload.infrastructure.postgres.model.Ownership

object OwnershipMapper {

    fun toEntity(ownership: Ownership): OwnershipEntity {
        return OwnershipEntity(
            customerId = ownership.customerId,
            animalId = ownership.animalId
        )
    }

    fun toJpaEntity(entity: OwnershipEntity): Ownership {
        return Ownership(
            customerId = entity.customerId,
            animalId = entity.animalId
        )
    }
}
