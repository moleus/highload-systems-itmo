package itmo.highload.repository

import itmo.highload.model.ImageToAnimal
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ImageToAnimalRepository : R2dbcRepository<ImageToAnimal, Int> {
    fun findByAnimalId(animalId: Int): Mono<ImageToAnimal>

    fun deleteAllByAnimalId(animalId: Int): Mono<Void>
}