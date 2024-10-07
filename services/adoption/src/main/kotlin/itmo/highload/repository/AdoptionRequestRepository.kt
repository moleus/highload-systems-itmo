package itmo.highload.repository

import itmo.highload.model.AdoptionRequest
import itmo.highload.api.dto.AdoptionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AdoptionRequestRepository : ReactiveCrudRepository<AdoptionRequest, Int> {
    fun findAllByStatus(status: AdoptionStatus, pageable: Pageable): Flux<AdoptionRequest>
    fun findByCustomerIdAndAnimalId(customerId: Int, animalId: Int): Mono<AdoptionRequest>
    fun findAllByCustomerId(customerId: Int, pageable: Pageable): Flux<AdoptionRequest>
}
