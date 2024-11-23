package itmo.highload.domain

import itmo.highload.infrastructure.postgres.model.AnimalToImage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ImageToAnimalRepository {
    fun findByAnimalId(animalId: Int): Flux<AnimalToImage>
    fun findByImageId(imageId: Int): Mono<AnimalToImage>
    fun deleteAnimalToImageByImageId(imageId: Int): Mono<Void>
    fun save(animalToImage: AnimalToImage): Mono<AnimalToImage>
    fun delete(animalToImage: AnimalToImage): Mono<Void>
}
