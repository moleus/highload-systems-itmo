package itmo.highload.domain.interactor

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.domain.AnimalRepository
import itmo.highload.domain.ImageToAnimalRepository
import itmo.highload.exceptions.ImageNotFoundException
import itmo.highload.infrastructure.postgres.model.AnimalToImage
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimalImageService(
    private val imageRepository: ImageToAnimalRepository,
    private val imageService: ImageService,
    private val animalRepository: AnimalRepository
) {
    private val logger = KotlinLogging.logger {}

    fun getImagesByAnimalId(animalId: Int, token: String): Flux<FileUrlResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                imageService.getImageUrlById(token, image.imageId)
                    .map { url -> FileUrlResponse(fileID = image.imageId, url = url.url) }
            }
    }

    fun saveImageByAnimalId(animalId: Int, token: String, imageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return animalRepository.findById(animalId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Animal with ID $animalId not found.")))
            .flatMap { _ ->
                logger.info { "Uploading image for animal with ID $animalId" }
                imageService.uploadImage(token, imageData)
                    .flatMap { uploadedFileResponse ->
                        logger.info { "Image uploaded with ID ${uploadedFileResponse.fileID}" }
                        val imageToAnimal = AnimalToImage(
                            animalId = animalId,
                            imageId = uploadedFileResponse.fileID
                        )
                        imageRepository.save(imageToAnimal)
                            .thenReturn(uploadedFileResponse)
                    }
            }
    }

    fun updateImageByImageId(imageId: Int, token: String, newImageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return imageRepository.findByImageId(imageId)
            .flatMap { existingImage ->
                imageService.updateImage(token, existingImage.imageId, newImageData)
                    .flatMap { updatedImage ->
                        Mono.just(updatedImage)
                    }
            }
            .switchIfEmpty(Mono.error(ImageNotFoundException("Image with id $imageId not found")))
    }


    fun deleteByImageId(imageId: Int, token: String): Mono<Void> {
        return imageRepository.findByImageId(imageId)
            .flatMap { image ->
                imageService.deleteImageById(token, image.imageId)
                    .then(imageRepository.deleteAnimalToImageByImageId(image.imageId))
            }
            .then()
    }


    fun deleteAllByAnimalId(animalId: Int, token: String): Mono<Void> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                imageService.deleteImageById(token, image.imageId)
                    .then(imageRepository.delete(image))
            }
            .then()
    }
}
