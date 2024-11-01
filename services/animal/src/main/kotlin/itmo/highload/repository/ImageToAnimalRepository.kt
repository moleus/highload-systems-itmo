package itmo.highload.repository

import itmo.highload.model.AnimalToImage
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface ImageToAnimalRepository : R2dbcRepository<AnimalToImage, Int> {
    fun findByAnimalId(animalId: Int): Mono<AnimalToImage>

}
