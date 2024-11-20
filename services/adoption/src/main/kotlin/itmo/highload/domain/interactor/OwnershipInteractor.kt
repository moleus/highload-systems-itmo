package itmo.highload.domain.interactor

import itmo.highload.infrastructure.postgres.OwnershipRepositoryImpl
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class OwnershipInteractor(private val ownershipRepository: OwnershipRepositoryImpl) {

    fun getAllAnimalsId(): Flux<Int> {
        return Flux.fromStream { ownershipRepository.findAll().stream() }
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.animalId }
    }
}