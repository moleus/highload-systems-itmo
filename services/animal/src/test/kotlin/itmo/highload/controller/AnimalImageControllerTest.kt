package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.domain.interactor.AnimalImageService
import itmo.highload.infrastructure.http.AnimalImageController
import org.junit.jupiter.api.Test
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AnimalImageControllerTest {

    private val animalImageService = mockk<AnimalImageService>()
    private val controller = AnimalImageController(animalImageService)

    @Test
    fun `getImagesByAnimalId - should return images for an animal`() {
        val animalId = 1
        val token = "validToken"
        val images = listOf(FileUrlResponse(fileID = 1, url = "http://example.com/image1.jpg"))

        every { animalImageService.getImagesByAnimalId(animalId, token) } returns Flux.fromIterable(images)

        StepVerifier.create(controller.getImagesByAnimalId(animalId, token))
            .expectNextMatches { it.url == "http://example.com/image1.jpg" }
            .verifyComplete()
    }

    @Test
    fun `addImageByAnimalId - should upload an image`() {
        val animalId = 1
        val token = "validToken"
        val filePart = mockk<FilePart>()
        val uploadedFileResponse = UploadedFileResponse(fileID = 123)

        every { animalImageService.saveImageByAnimalId(animalId, token, any()) } returns Mono.just(uploadedFileResponse)

        StepVerifier.create(controller.addImageByAnimalId(animalId, token, Mono.just(filePart)))
            .expectNextMatches { it.fileID == 123 }
            .verifyComplete()
    }

    @Test
    fun `updateImageById - should update an existing image`() {
        val imageId = 123
        val token = "validToken"
        val filePart = mockk<FilePart>()
        val updatedFileResponse = UploadedFileResponse(fileID = 123)

        every { animalImageService.updateImageByImageId(imageId, token, any()) } returns Mono.just(updatedFileResponse)

        StepVerifier.create(controller.updateImageById(imageId, token, Mono.just(filePart)))
            .expectNextMatches { it.fileID == 123 }
            .verifyComplete()
    }

    @Test
    fun `deleteImageById - should delete an image`() {
        val imageId = 123
        val token = "validToken"

        every { animalImageService.deleteByImageId(imageId, token) } returns Mono.empty()

        StepVerifier.create(controller.deleteImageById(imageId, token))
            .verifyComplete()
    }
}
