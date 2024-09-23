@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.User
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.service.mapper.AdoptionRequestMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdoptionRequestService(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val customerRepository: CustomerRepository,
    private val animalRepository: AnimalRepository
) {
    fun save(customerId: Int, animalId: Int): AdoptionRequest {
        require(adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId) == null) {
            "An adoption request already exists for customer ID: $customerId and animal ID: $animalId"
        }
        val customer = customerRepository.findById(customerId).orElseThrow()
        val animal = animalRepository.findById(animalId).orElseThrow()

        val adoptionRequest = AdoptionRequestMapper.toEntity(customer, animal, AdoptionStatus.PENDING)

        return adoptionRequestRepository.save(adoptionRequest)
    }

    fun update(manager: User, request: UpdateAdoptionRequestStatusDto): AdoptionRequest {
        val adoptionRequest = adoptionRequestRepository.findById(request.id).orElseThrow()

        adoptionRequest.status = request.status
        adoptionRequest.manager = manager

        return adoptionRequestRepository.save(adoptionRequest)
    }

    fun delete(customerId: Int, animalId: Int) {
        val adoptionRequest = adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
            ?: throw NoSuchElementException("Adoption request not found")
        adoptionRequestRepository.delete(adoptionRequest)
    }

    fun getAll(status: AdoptionStatus?, pageable: Pageable): Page<AdoptionRequest> {
        val requestsPage = if (status != null) {
            adoptionRequestRepository.findAllByStatus(status, pageable)
        } else {
            adoptionRequestRepository.findAll(pageable)
        }
        return requestsPage
    }

    fun getAllByCustomer(customerId: Int, pageable: Pageable): Page<AdoptionRequest> {
        val requestsPage = adoptionRequestRepository.findByCustomerId(customerId, pageable)
        return requestsPage
    }

    fun getAllStatuses(): List<AdoptionStatus> {
        return adoptionRequestRepository.findAllUniqueAdoptionStatuses()
    }
}
