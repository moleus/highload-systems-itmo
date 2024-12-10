package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.Animal
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AnimalRepository {
    fun findByIdNotIn(ids: List<Int>): Flux<Animal>
    fun findByNameAndIdNotIn(name: String, ids: List<Int>): Flux<Animal>
    fun save(animal: Animal): Mono<Animal>
    fun delete(animal: Animal): Mono<Void>
    fun findById(id: Int): Mono<Animal>
    fun findAll(): Flux<Animal>
}
