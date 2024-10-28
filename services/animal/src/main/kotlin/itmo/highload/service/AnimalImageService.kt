package itmo.highload.service

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.repository.ImageToAnimalRepository
import reactor.core.publisher.Mono

class AnimalImageService(
    private val imageRepository: ImageToAnimalRepository,
    private val imageService: ImageService
) {
    fun getImageByAnimalId(animalId: Int, token: String): Mono<FileUrlResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                Mono.fromCallable { imageService.getImageUrlById(token, image.imageId) }
            }
    }

    fun saveImageByAnimalId(animalId: Int, token: String) {
        imageRepository.findByAnimalId(animalId).flatMap {  }

    }

    fun updateImageByAnimalId(animalId: Int, token: String) {
        imageRepository.findByAnimalId(animalId).flatMap {
            imageService.
        }

    }

    fun deleteAllByAnimalId(animalId: Int, token: String): Mono<Void> {
        return imageRepository.deleteAllByAnimalId(animalId)
            .then(imageService.deleteImageById(token, ))
    }
}
