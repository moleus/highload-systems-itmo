package itmo.highload.repository

import itmo.highload.model.Ownership
import itmo.highload.model.OwnershipId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OwnershipRepository : JpaRepository<Ownership, OwnershipId> {
    fun findByCustomerIdAndAnimalId(customerId: Int, animal: Int): Optional<Ownership>
}
