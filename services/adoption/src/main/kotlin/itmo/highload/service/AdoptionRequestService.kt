package itmo.highload.service

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.exceptions.InvalidAdoptionRequestStatusException
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.AdoptionRequestMapper
import itmo.highload.model.Ownership
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.OwnershipRepository
import itmo.highload.service.exception.EntityAlreadyExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdoptionRequestService(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val ownershipRepository: OwnershipRepository
) {
    private val logger = LoggerFactory.getLogger(AdoptionRequestService::class.java)

    fun save(customerId: Int, animalId: Int): AdoptionRequest {
        if (adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId) != null) {
            logger.error("Adoption request already exists for customer ID: $customerId and animal ID: $animalId")
            throw EntityAlreadyExistsException(
                "An adoption request already exists " +
                        "for customer ID: $customerId and animal ID: $animalId"
            )
        }
        val adoptionRequest = AdoptionRequestMapper.toEntity(customerId, animalId, AdoptionStatus.PENDING)
        logger.error("Saving adoption request for customer ID: $customerId and animal ID: $animalId")

        return adoptionRequestRepository.save(adoptionRequest)
    }

    @Transactional
    fun update(managerId: Int, request: UpdateAdoptionRequestStatusDto): AdoptionRequest {
        val adoptionRequest = adoptionRequestRepository.findById(request.id!!).orElseThrow {
            EntityNotFoundException("Adoption request not found")
        }

        adoptionRequest.status = request.status!!
        adoptionRequest.managerId = managerId

        if (request.status == AdoptionStatus.APPROVED) {
            val ownership = Ownership(
                customerId = adoptionRequest.customerId,
                animalId = adoptionRequest.animalId,
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
        return adoptionRequestRepository.findAllByCustomerId(customerId, pageable)
    }

    fun getAllStatuses(): List<AdoptionStatus> {
        return AdoptionStatus.entries
    }
}
