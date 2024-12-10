package itmo.highload.domain

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.infrastructure.postgres.model.AdoptionRequest
import java.util.*

interface AdoptionRequestRepository {
    fun findAllByStatus(status: AdoptionStatus): List<AdoptionRequest>
    fun findByCustomerIdAndAnimalId(customerId: Int, animalId: Int): Optional<AdoptionRequest>
    fun findAllByCustomerId(customerId: Int): List<AdoptionRequest>
    fun findAllByCustomerIdAndStatus(customerId: Int, status: AdoptionStatus): List<AdoptionRequest>
    fun save(adoptionRequest: AdoptionRequest): AdoptionRequest
    fun delete(adoptionRequest: AdoptionRequest)
    fun findById(id: Int): Optional<AdoptionRequest>
    fun findAll(): List<AdoptionRequest>
}
