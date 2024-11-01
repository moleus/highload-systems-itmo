package itmo.highload.service

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.model.AnimalToImage
import itmo.highload.repository.ImageToAnimalRepository
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AnimalImageService(
    private val imageRepository: ImageToAnimalRepository,
    private val imageService: ImageService
) {
    fun getImageByAnimalId(animalId: Int, token: String): Mono<FileUrlResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { image ->
                imageService.getImageUrlById(token, image.imageId)
            }
    }


    fun saveImageByAnimalId(animalId: Int, token: String, imageData: FilePart): Mono<UploadedFileResponse> {
        return imageService.uploadImage(token, Mono.just(imageData))
            .flatMap { uploadedFileResponse ->
                val imageToAnimal = AnimalToImage(
                    animalId = animalId,
                    imageId = uploadedFileResponse.fileID
                )
                imageRepository.save(imageToAnimal).thenReturn(uploadedFileResponse)
            }
    }

    fun updateImageByAnimalId(animalId: Int, token: String, newImageData: FilePart): Mono<UploadedFileResponse> {
        return imageRepository.findByAnimalId(animalId)
            .flatMap { existingImage ->
                imageService.deleteImageById(token, existingImage.imageId)
                    .then(imageService.uploadImage(token, Mono.just(newImageData)))
            }
            .flatMap { newUploadedImage ->
                val updatedImage = AnimalToImage(
                    animalId = animalId,
                    imageId = newUploadedImage.fileID
                )
                imageRepository.save(updatedImage).thenReturn(newUploadedImage)
            }
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
