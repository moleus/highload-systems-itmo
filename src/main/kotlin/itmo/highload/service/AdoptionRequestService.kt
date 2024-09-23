@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.User
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.service.exception.EntityAlreadyExistsException
import itmo.highload.service.exception.InvalidAdoptionRequestStatusException
import itmo.highload.mapper.AdoptionRequestMapper
import itmo.highload.model.Ownership
import itmo.highload.repository.OwnershipRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdoptionRequestService(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val customerRepository: CustomerRepository,
    private val animalService: AnimalService,
    private val ownershipRepository: OwnershipRepository
) {

    fun save(customerId: Int, animalId: Int): AdoptionRequest {
        if (adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId) != null) {
            throw EntityAlreadyExistsException(
                "An adoption request already exists " +
                        "for customer ID: $customerId and animal ID: $animalId"
            )
        }
        val customer = customerRepository.findById(customerId).orElseThrow {
            EntityNotFoundException("Customer not found")
        }
        val animal = animalService.get(animalId)

        val adoptionRequest = AdoptionRequestMapper.toEntity(customer, animal, AdoptionStatus.PENDING)

        return adoptionRequestRepository.save(adoptionRequest)
    }

    @Transactional
    fun update(manager: User, request: UpdateAdoptionRequestStatusDto): AdoptionRequest {
        val adoptionRequest = adoptionRequestRepository.findById(request.id).orElseThrow {
            EntityNotFoundException("Adoption request not found")
        }

        adoptionRequest.status = request.status
        adoptionRequest.manager = manager

        if (request.status == AdoptionStatus.APPROVED) {
            val ownership = Ownership(
                customer = adoptionRequest.customer,
                animal = adoptionRequest.animal,
            )

            ownershipRepository.save(ownership)
        }

        return adoptionRequestRepository.save(adoptionRequest)
    }

    fun delete(customerId: Int, animalId: Int) {
        val adoptionRequest = adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
            ?: throw EntityNotFoundException("Adoption request not found")
        if (adoptionRequest.status != AdoptionStatus.PENDING) {
            throw InvalidAdoptionRequestStatusException(
                "Cannot delete adoption " +
                        "request with status: ${adoptionRequest.status}"
            )
        }
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
