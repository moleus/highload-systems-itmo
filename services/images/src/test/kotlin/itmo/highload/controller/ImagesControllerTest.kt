package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.domain.interactor.ImagesService
import itmo.highload.infrastructure.http.ImagesController
import itmo.highload.infrastructure.minio.model.S3ObjectRef
import org.junit.jupiter.api.Test
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ImagesControllerTest {

    private val imagesService = mockk<ImagesService>()
    private val controller = ImagesController(imagesService)

    @Test
    fun `getImageUrlById - should return image URL when image exists`() {
        val imageId = 1
        val expectedUrl = "http://example.com/image1.jpg"

        val s3ObjectRef = S3ObjectRef(id = 1, bucket = "images", key = "image1.jpg")

        every { imagesService.getImageById(imageId) } returns Mono.just(s3ObjectRef)
        every { imagesService.constructPublicEndpointFromPath(s3ObjectRef) } returns expectedUrl

        StepVerifier.create(controller.getImageUrlById(imageId))
            .expectNextMatches { it.url == expectedUrl }
            .verifyComplete()
    }

    @Test
    fun `uploadImage - should return uploaded file response`() {
        val filePart = mockk<FilePart>()
        val s3ObjectRef = S3ObjectRef(id = 1, bucket = "images", key = "image1.jpg")

        every { imagesService.saveImage(filePart) } returns Mono.just(s3ObjectRef)
        every { imagesService.constructPublicEndpointFromPath(s3ObjectRef) } returns "http://example.com/image1.jpg"

        StepVerifier.create(controller.uploadImage(Mono.just(filePart)))
            .expectNextMatches { it.fileID == 1 }
            .verifyComplete()
    }

    @Test
    fun `deleteImageById - should delete image when image exists`() {
        val imageId = 1

        every { imagesService.deleteImageById(imageId) } returns Mono.empty()

        StepVerifier.create(controller.deleteImageById(imageId))
            .verifyComplete()
    }

    @Test
    fun `updateImageById - should update image successfully`() {
        val imageId = 1
        val filePart = mockk<FilePart>()
        val updatedFileUrl = "http://example.com/updated-image.jpg"

        // Создаем объект S3ObjectRef вместо FileUrlResponse
        val s3ObjectRef = S3ObjectRef(id = imageId, bucket = "images", key = "updated-image.jpg")

        // Мокаем метод updateImageById, чтобы он возвращал S3ObjectRef
        every { imagesService.updateImageById(imageId, filePart) } returns Mono.just(s3ObjectRef)

        // Мокаем метод для создания публичного URL
        every { imagesService.constructPublicEndpointFromPath(s3ObjectRef) } returns updatedFileUrl

        // Выполняем тест
        StepVerifier.create(controller.updateImageById(imageId, Mono.just(filePart)))
            .expectNextMatches { it.url == updatedFileUrl }
            .verifyComplete()
    }
}
