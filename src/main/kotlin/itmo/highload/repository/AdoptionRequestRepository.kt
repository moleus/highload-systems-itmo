package itmo.highload.repository

import itmo.highload.model.AdoptionRequest
import itmo.highload.model.enum.AdoptionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AdoptionRequestRepository : JpaRepository<AdoptionRequest, Int> {
    fun findAllByStatus(status: AdoptionStatus, pageable: Pageable): Page<AdoptionRequest>
    fun findByCustomerIdAndAnimalId(customerId: Int, animalId: Int): AdoptionRequest?
    fun findByCustomerId(customerId: Int, pageable: Pageable): Page<AdoptionRequest>

    @Query("SELECT DISTINCT a.status FROM AdoptionRequest a")
    fun findAllUniqueAdoptionStatuses(): List<AdoptionStatus>
}
