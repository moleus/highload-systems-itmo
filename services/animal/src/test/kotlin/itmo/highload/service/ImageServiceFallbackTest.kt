package itmo.highload.service

import itmo.highload.exceptions.ServiceUnavailableException
import org.junit.jupiter.api.Test
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ImageServiceFallbackTest {

    private val imageServiceFallback = ImageServiceFallback()

    @Test
    fun `getImageUrlById should return fallback image URL`() {
        val token = "validToken"
        val imageId = 1
        val expectedUrl = "https://http.cat/images/503.jpg"

        val response = imageServiceFallback.getImageUrlById(token, imageId)

        StepVerifier.create(response)
            .expectNextMatches { it.url == expectedUrl && it.fileID == imageId }
            .verifyComplete()
    }

    @Test
    fun `uploadImage should return error when service is unavailable`() {
        val token = "validToken"
        val fileParts: Mono<FilePart> = Mono.empty()

        val response = imageServiceFallback.uploadImage(token, fileParts)

        StepVerifier.create(response)
            .expectError(ServiceUnavailableException::class.java)
            .verify()
    }

    @Test
    fun `deleteImageById should return error when service is unavailable`() {
        val token = "validToken"
        val imageId = 1

        val response = imageServiceFallback.deleteImageById(token, imageId)

        StepVerifier.create(response)
            .expectError(ServiceUnavailableException::class.java)
            .verify()
    }

    @Test
    fun `updateImage should return error when service is unavailable`() {
        val token = "validToken"
        val imageId = 1
        val fileParts: Mono<FilePart> = Mono.empty()

        val response = imageServiceFallback.updateImage(token, imageId, fileParts)

        StepVerifier.create(response)
            .expectError(ServiceUnavailableException::class.java)
            .verify()
    }
}
