package itmo.highload.domain.interactor

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.domain.AdoptionRequestProducer
import itmo.highload.domain.AdoptionRequestRepository
import itmo.highload.domain.OwnershipRepository
import itmo.highload.domain.entity.AdoptionRequestEntity
import itmo.highload.domain.entity.OwnershipEntity
import itmo.highload.domain.mapper.AdoptionRequestMapper
import itmo.highload.domain.mapper.OwnershipMapper
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.InvalidAdoptionRequestStatusException
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class AdoptionRequestInteractor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val ownershipRepository: OwnershipRepository,
    private val adoptionRequestProducer: AdoptionRequestProducer
) {
    companion object {
        private const val ADOPTION_REQUEST_NOT_FOUND = "Adoption request not found"
    }

    private val logger = LoggerFactory.getLogger(AdoptionRequestInteractor::class.java)

    fun save(customerId: Int, animalId: Int): Mono<AdoptionRequestEntity> {
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
                val adoptionRequest = AdoptionRequestMapper.toJpaEntity(customerId, animalId, AdoptionStatus.PENDING)
                logger.info("Saving adoption request for customer ID: $customerId and animal ID: $animalId")
                Mono.fromCallable { AdoptionRequestMapper.toEntity(adoptionRequestRepository.save(adoptionRequest)) }
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess {
                        val message = AdoptionRequestMapper.toResponse(adoptionRequest)
                        Mono.fromCallable { adoptionRequestProducer.sendMessageToAdoptionRequestCreatedTopic(message) }
                            .subscribeOn(Schedulers.boundedElastic())
                            .onErrorContinue { error, _ ->
                                logger.error("Failed to send message to Kafka: ${error.message}")
                            }
                            .subscribe()                    }
            }
        }
    }

    fun update(managerId: Int, request: UpdateAdoptionRequestStatusDto): Mono<AdoptionRequestEntity> {
        return Mono.fromCallable {
            adoptionRequestRepository.findById(request.id!!).orElseThrow {
                EntityNotFoundException(ADOPTION_REQUEST_NOT_FOUND)
            }
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { adoptionRequest ->
            adoptionRequest.status = request.status!!
            adoptionRequest.managerId = managerId

            val saveMono = if (request.status == AdoptionStatus.APPROVED) {
                Mono.fromCallable {
                    val ownership = OwnershipEntity(
                        customerId = adoptionRequest.customerId,
                        animalId = adoptionRequest.animalId,
                    )
                    ownershipRepository.save(OwnershipMapper.toJpaEntity(ownership))
                    AdoptionRequestMapper.toEntity(adoptionRequestRepository.save(adoptionRequest))
                }.subscribeOn(Schedulers.boundedElastic())
            } else {
                Mono.fromCallable { AdoptionRequestMapper.toEntity(adoptionRequestRepository.save(adoptionRequest)) }
                    .subscribeOn(Schedulers.boundedElastic())
            }
            saveMono.doOnSuccess {
                val message = AdoptionRequestMapper.toResponse(adoptionRequest)
                Mono.fromCallable { adoptionRequestProducer.sendMessageToAdoptionRequestChangedTopic(message) }
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorContinue { error, _ ->
                        logger.error("Failed to send message to Kafka: ${error.message}")
                    }
                    .subscribe()
            }
        }
    }


    fun delete(customerId: Int, animalId: Int): Mono<Void> {
        return Mono.fromCallable {
            adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId)
                .orElseThrow { EntityNotFoundException(ADOPTION_REQUEST_NOT_FOUND) }
        }.subscribeOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(EntityNotFoundException(ADOPTION_REQUEST_NOT_FOUND)))
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

    fun getAll(status: AdoptionStatus?): Flux<AdoptionRequestEntity> {
        return (status?.let {
            Flux.fromStream { adoptionRequestRepository.findAllByStatus(it).stream() }
                .subscribeOn(Schedulers.boundedElastic())
                .map { req -> AdoptionRequestMapper.toEntity(req) }
        } ?: Flux.fromStream { adoptionRequestRepository.findAll().stream() }
            .subscribeOn(Schedulers.boundedElastic())
            .map { req -> AdoptionRequestMapper.toEntity(req) })
    }

    fun getAllByCustomer(customerId: Int, status: AdoptionStatus?): Flux<AdoptionRequestEntity> {
        return (status?.let {
            Flux.fromStream { adoptionRequestRepository.findAllByCustomerIdAndStatus(customerId, it).stream() }
                .subscribeOn(Schedulers.boundedElastic())
                .map { req -> AdoptionRequestMapper.toEntity(req) }
        } ?: Flux.fromStream { adoptionRequestRepository.findAllByCustomerId(customerId).stream() }
            .subscribeOn(Schedulers.boundedElastic())
            .map { req -> AdoptionRequestMapper.toEntity(req) })
    }

    fun getAllStatuses(): Flux<AdoptionStatus> {
        return Flux.fromIterable(AdoptionStatus.entries)
    }
}
