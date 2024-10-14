package itmo.highload.repository

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.model.AdoptionRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdoptionRequestRepository : JpaRepository<AdoptionRequest, Int> {
    fun findAllByStatus(status: AdoptionStatus): List<AdoptionRequest>
    fun findByCustomerIdAndAnimalId(customerId: Int, animalId: Int): Optional<AdoptionRequest>
    fun findAllByCustomerId(customerId: Int): List<AdoptionRequest>
}
