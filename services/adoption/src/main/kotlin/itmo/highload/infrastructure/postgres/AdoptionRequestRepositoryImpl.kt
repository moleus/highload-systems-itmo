package itmo.highload.infrastructure.postgres

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.domain.AdoptionRequestRepository
import itmo.highload.infrastructure.postgres.model.AdoptionRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdoptionRequestRepositoryImpl : JpaRepository<AdoptionRequest, Int>, AdoptionRequestRepository {
    override fun findAllByStatus(status: AdoptionStatus): List<AdoptionRequest>
    override fun findByCustomerIdAndAnimalId(customerId: Int, animalId: Int): Optional<AdoptionRequest>
    override fun findAllByCustomerId(customerId: Int): List<AdoptionRequest>
    override fun findAllByCustomerIdAndStatus(customerId: Int, status: AdoptionStatus): List<AdoptionRequest>

}
