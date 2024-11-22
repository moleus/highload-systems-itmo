package itmo.highload.infrastructure.postgres

import itmo.highload.domain.OwnershipRepository
import itmo.highload.infrastructure.postgres.model.Ownership
import itmo.highload.infrastructure.postgres.model.OwnershipId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OwnershipRepositoryImpl : JpaRepository<Ownership, OwnershipId>, OwnershipRepository
