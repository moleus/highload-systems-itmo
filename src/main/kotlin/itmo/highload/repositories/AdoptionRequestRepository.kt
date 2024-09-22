package itmo.highload.repositories

import itmo.highload.model.AdoptionRequest
import itmo.highload.model.enums.AdoptionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdoptionRequestRepository : JpaRepository<AdoptionRequest, Int> {
    fun findByStatus(status: AdoptionStatus, pageable: Pageable): Page<AdoptionRequest>
}
