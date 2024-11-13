package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.Ownership

interface OwnershipRepository {
    fun save(ownership: Ownership): Ownership
}
