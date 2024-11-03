package itmo.highload.service

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.exceptions.ImageNotFoundException
import itmo.highload.model.AnimalToImage
import itmo.highload.repository.ImageToAnimalRepository
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimalImageService(
    private val imageRepository: ImageToAnimalRepository,
    private val imageService: ImageService,
) {
    private val logger = KotlinLogging.logger { }

    fun getImagesByAnimalId(animalId: Int, token: String): Flux<FileUrlResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                imageService.getImageUrlById(token, image.imageId)
                    .map { url -> FileUrlResponse(fileID = image.imageId, url = url.url) }
            }
    }


    fun saveImageByAnimalId(animalId: Int, token: String, imageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return imageService.uploadImage(token, imageData)
            .flatMap { uploadedFileResponse ->
                val imageToAnimal = AnimalToImage(
                    animalId = animalId,
                    imageId = uploadedFileResponse.fileID
                )
                imageRepository.save(imageToAnimal).thenReturn(uploadedFileResponse)
            }
    }

    fun updateImageByImageId(imageId: Int, token: String, newImageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return imageRepository.findByImageId(imageId)
            .flatMap { existingImage ->
                imageService.deleteImageById(token, existingImage.imageId)
                    .then(imageRepository.delete(existingImage))
                    .then(imageService.uploadImage(token, newImageData))
                    .flatMap { newUploadedImage ->
                        val updatedImage = AnimalToImage(
                            animalId = existingImage.animalId,
                            imageId = newUploadedImage.fileID
                        )
                        imageRepository.save(updatedImage).thenReturn(newUploadedImage)
                    }
            }
            .switchIfEmpty(Mono.error(ImageNotFoundException("Image with id $imageId not found")))
    }


    fun deleteByImageId(imageId: Int, token: String): Mono<Void> {
        return imageRepository.findByImageId(imageId)
            .flatMap { image ->
                logger.info{"Изображение найдено: $image, ${image.imageId}. Начинаем удаление изображения из сервиса."}
                imageService.deleteImageById(token, image.imageId)
                    .doOnSuccess { logger.info("Изображение с ID: ${image.imageId} успешно удалено из сервиса.") }
                    .then(imageRepository.delete(image))
                    .doOnSuccess { logger.info("Изображение с ID: ${image.imageId} успешно удалено из репозитория.") }
            }
            .then()
    }

    fun getAllImagesIdByAnimalId(animalId: Int): Flux<Int> {
        return imageRepository.findByAnimalId(animalId)
            .map { image -> image.imageId }
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
