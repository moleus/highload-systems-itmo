package itmo.highload.service

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.exceptions.ImageNotFoundException
import itmo.highload.model.AnimalToImage
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.ImageToAnimalRepository
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

    fun getImagesByAnimalId(animalId: Int, token: String): Flux<FileUrlResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                imageService.getImageUrlById(token, image.imageId)
                    .map { url -> FileUrlResponse(fileID = image.imageId, url = url.url) }
            }
    }

    fun saveImageByAnimalId(animalId: Int, token: String, imageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return animalRepository.findById(animalId)
            .flatMap { _ ->
                imageService.uploadImage(token, imageData)
                    .flatMap { uploadedFileResponse ->
                        val imageToAnimal = AnimalToImage(
                            animalId = animalId,
                            imageId = uploadedFileResponse.fileID
                        )
                        imageRepository.save(imageToAnimal)
                            .thenReturn(uploadedFileResponse)
                    }
            }
            .switchIfEmpty(Mono.error(EntityNotFoundException("Animal with ID $animalId not found.")))
    }


//    fun updateImageByImageId(imageId: Int, token: String, newImageData: Mono<FilePart>): Mono<UploadedFileResponse> {
//        return imageRepository.findByImageId(imageId)
//            .flatMap { existingImage ->
//                imageService.deleteImageById(token, existingImage.imageId)
//                    .then(imageRepository.deleteAnimalToImageByImageId(existingImage.imageId))
//                    .then(imageService.uploadImage(token, newImageData))
//                    .flatMap { newUploadedImage ->
//                        val updatedImage = AnimalToImage(
//                            animalId = existingImage.animalId,
//                            imageId = newUploadedImage.fileID
//                        )
//                        imageRepository.save(updatedImage).thenReturn(newUploadedImage)
//                    }
//            }
//            .switchIfEmpty(Mono.error(ImageNotFoundException("Image with id $imageId not found")))
//    }

    fun updateImageByImageId(imageId: Int, token: String, newImageData: Mono<FilePart>): Mono<UploadedFileResponse> {
        return imageRepository.findByImageId(imageId)
            .flatMap { existingImage ->
                // Вызываем метод updateImage для обновления существующего изображения по его ID
                imageService.updateImage(token, existingImage.imageId, newImageData)
                    .flatMap { updatedImage ->
                        // Возвращаем обновленный ответ после успешного обновления
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
