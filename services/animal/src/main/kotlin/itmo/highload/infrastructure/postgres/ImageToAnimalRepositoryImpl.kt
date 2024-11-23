package itmo.highload.infrastructure.postgres

import itmo.highload.domain.ImageToAnimalRepository
import itmo.highload.infrastructure.postgres.model.AnimalToImage
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ImageToAnimalRepositoryImpl : R2dbcRepository<AnimalToImage, Int>, ImageToAnimalRepository {
    override fun findByAnimalId(animalId: Int): Flux<AnimalToImage>

    override fun findByImageId(imageId: Int): Mono<AnimalToImage>

    override fun deleteAnimalToImageByImageId(imageId: Int): Mono<Void>
}
