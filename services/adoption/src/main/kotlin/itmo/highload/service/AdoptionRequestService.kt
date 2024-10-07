package itmo.highload.service

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.exceptions.InvalidAdoptionRequestStatusException
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.AdoptionRequestMapper
import itmo.highload.model.Ownership
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.OwnershipRepository
import itmo.highload.service.exception.EntityAlreadyExistsException
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AdoptionRequestService(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val ownershipRepository: OwnershipRepository
) {
    private val logger = LoggerFactory.getLogger(AdoptionRequestService::class.java)

    fun save(customerId: Int, animalId: Int): Mono<AdoptionRequestResponse> {
        return adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId).flatMap<AdoptionRequest> {
            logger.error("Adoption request already exists for customer ID: $customerId and animal ID: $animalId")
            Mono.error(
                EntityAlreadyExistsException(
                    "An adoption request already exists " + "for customer ID: $customerId and animal ID: $animalId"
                )
            )
        }.switchIfEmpty(Mono.defer {
            val adoptionRequest = AdoptionRequestMapper.toEntity(customerId, animalId, AdoptionStatus.PENDING)
            logger.error("Saving adoption request for customer ID: $customerId and animal ID: $animalId")
            adoptionRequestRepository.save(adoptionRequest)
        }).map { AdoptionRequestMapper.toResponse(it) }
    }

    fun update(managerId: Int, request: UpdateAdoptionRequestStatusDto): Mono<AdoptionRequestResponse> {
        return adoptionRequestRepository.findById(request.id!!)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Adoption request not found")))
            .flatMap { adoptionRequest ->
                adoptionRequest.status = request.status!!
                adoptionRequest.managerId = managerId

                val saveMono = if (request.status == AdoptionStatus.APPROVED) {
                    val ownership = Ownership(
                        customerId = adoptionRequest.customerId,
                        animalId = adoptionRequest.animalId,
                    )
                    ownershipRepository.save(ownership).then(adoptionRequestRepository.save(adoptionRequest))
                } else {
                    adoptionRequestRepository.save(adoptionRequest)
                }
                saveMono
            }.map { AdoptionRequestMapper.toResponse(it) }
    }

    fun delete(customerId: Int, animalId: Int): Mono<Void> {
        return adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Adoption request not found")))
            .flatMap { adoptionRequest ->
                if (adoptionRequest.status != AdoptionStatus.PENDING) {
                    return@flatMap Mono.error<Void>(
                        InvalidAdoptionRequestStatusException(
                            "Cannot delete adoption request with status: ${adoptionRequest.status}"
                        )
                    )
                }
                adoptionRequestRepository.delete(adoptionRequest)
            }
    }

    fun getAll(status: AdoptionStatus?, pageable: Pageable): Flux<AdoptionRequestResponse> {
        return (status?.let { adoptionRequestRepository.findAllByStatus(it, pageable) }
            ?: adoptionRequestRepository.findAll())
            .map { AdoptionRequestMapper.toResponse(it) }
    }

    fun getAllByCustomer(customerId: Int, pageable: Pageable): Flux<AdoptionRequestResponse> {
        return adoptionRequestRepository.findAllByCustomerId(customerId, pageable)
            .map { AdoptionRequestMapper.toResponse(it) }
    }

    fun getAllStatuses(): Flux<AdoptionStatus> {
        return Flux.fromIterable(AdoptionStatus.entries)
    }
}
