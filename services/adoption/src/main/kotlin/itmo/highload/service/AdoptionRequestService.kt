package itmo.highload.service

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.InvalidAdoptionRequestStatusException
import itmo.highload.kafka.AdoptionRequestProducer
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.AdoptionRequestMapper
import itmo.highload.model.Ownership
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.OwnershipRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class AdoptionRequestService(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val ownershipRepository: OwnershipRepository,
    private val adoptionRequestProducer: AdoptionRequestProducer
) {
    private val logger = LoggerFactory.getLogger(AdoptionRequestService::class.java)

    fun save(customerId: Int, animalId: Int): Mono<AdoptionRequest> {
        return Mono.fromCallable {
            adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { existingRequest ->
            if (existingRequest.isPresent) {
                logger.error("Adoption request already exists for customer ID: $customerId and animal ID: $animalId")
                Mono.error(
                    EntityAlreadyExistsException(
                        "An adoption request already exists for customer ID: $customerId and animal ID: $animalId"
                    )
                )
            } else {
                val adoptionRequest = AdoptionRequestMapper.toEntity(customerId, animalId, AdoptionStatus.PENDING)
                logger.info("Saving adoption request for customer ID: $customerId and animal ID: $animalId")
                Mono.fromCallable { adoptionRequestRepository.save(adoptionRequest) }
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess {
                        val message = AdoptionRequestMapper.toResponse(adoptionRequest)
                        adoptionRequestProducer.sendMessageToAdoptionRequestCreatedTopic(message)
                    }
            }
        }
    }

    fun update(managerId: Int, request: UpdateAdoptionRequestStatusDto): Mono<AdoptionRequest> {
        return Mono.fromCallable {
            adoptionRequestRepository.findById(request.id!!).orElseThrow {
                EntityNotFoundException("Adoption request not found")
            }
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { adoptionRequest ->
            adoptionRequest.status = request.status!!
            adoptionRequest.managerId = managerId

            val saveMono = if (request.status == AdoptionStatus.APPROVED) {
                Mono.fromCallable {
                    val ownership = Ownership(
                        customerId = adoptionRequest.customerId,
                        animalId = adoptionRequest.animalId,
                    )
                    ownershipRepository.save(ownership)
                    adoptionRequestRepository.save(adoptionRequest)
                }.subscribeOn(Schedulers.boundedElastic())
            } else {
                Mono.fromCallable { adoptionRequestRepository.save(adoptionRequest) }
                    .subscribeOn(Schedulers.boundedElastic())
            }
            saveMono.doOnSuccess {
                val message = AdoptionRequestMapper.toResponse(adoptionRequest)
                adoptionRequestProducer.sendMessageToAdoptionRequestChangedTopic(message)
            }
        }
    }


    fun delete(customerId: Int, animalId: Int): Mono<Void> {
        return Mono.fromCallable {
            adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
                .orElseThrow { EntityNotFoundException("Adoption request not found") }
        }.subscribeOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(EntityNotFoundException("Adoption request not found")))
            .flatMap { adoptionRequest ->
                if (adoptionRequest.status != AdoptionStatus.PENDING) {
                    return@flatMap Mono.error<Void>(
                        InvalidAdoptionRequestStatusException(
                            "Cannot delete adoption request with status: ${adoptionRequest.status}"
                        )
                    )
                }
                Mono.fromCallable {
                    adoptionRequestRepository.delete(adoptionRequest)
                }.subscribeOn(Schedulers.boundedElastic()).then()
            }
    }

    fun getAll(status: AdoptionStatus?): Flux<AdoptionRequest> {
        return (status?.let {
            Flux.fromStream { adoptionRequestRepository.findAllByStatus(it).stream() }
                .subscribeOn(Schedulers.boundedElastic())
        } ?: Flux.fromStream { adoptionRequestRepository.findAll().stream() }
            .subscribeOn(Schedulers.boundedElastic()))
    }

    fun getAllByCustomer(customerId: Int): Flux<AdoptionRequest> {
        return Flux.fromStream { adoptionRequestRepository.findAllByCustomerId(customerId).stream() }
            .subscribeOn(Schedulers.boundedElastic())
    }

    fun getAllStatuses(): Flux<AdoptionStatus> {
        return Flux.fromIterable(AdoptionStatus.entries)
    }
}
