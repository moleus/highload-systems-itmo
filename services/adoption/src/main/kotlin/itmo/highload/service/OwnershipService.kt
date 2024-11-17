package itmo.highload.service

import itmo.highload.repository.OwnershipRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class OwnershipService(private val ownershipRepository: OwnershipRepository) {

    fun getAllAnimalsId(): Flux<Int> {
        return Flux.fromStream { ownershipRepository.findAll().stream() }
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.animalId }
    }
}
