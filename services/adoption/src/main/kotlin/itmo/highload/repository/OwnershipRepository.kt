package itmo.highload.repository

import itmo.highload.model.Ownership
import itmo.highload.model.OwnershipId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface OwnershipRepository : ReactiveCrudRepository<Ownership, OwnershipId> {
    fun findByCustomerIdAndAnimalId(customerId: Int, animal: Int): Mono<Ownership?>
}
