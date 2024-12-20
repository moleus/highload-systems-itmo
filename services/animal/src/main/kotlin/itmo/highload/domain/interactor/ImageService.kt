package itmo.highload.domain.interactor

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.exceptions.ServiceUnavailableException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Mono

@ReactiveFeignClient(
    value = "image-service",
    url = "http://\${services.endpoints.images:localhost:8090}/api/v1",
    fallback = ImageServiceFallback::class
)
interface ImageService {
    @GetMapping("/images/{id}")
    fun getImageUrlById(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: Int
    ): Mono<FileUrlResponse>

    @PostMapping("/images/upload")
    fun uploadImage(
        @RequestHeader("Authorization") token: String,
        fileParts: Mono<FilePart>
    ): Mono<UploadedFileResponse>

    @DeleteMapping("/images/{id}")
    fun deleteImageById(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: Int
    ): Mono<Void>

    @PutMapping("/images/{id}")
    fun updateImage(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: Int,
        fileParts: Mono<FilePart>
    ): Mono<UploadedFileResponse>

}

@Component
class ImageServiceFallback : ImageService {

    override fun getImageUrlById(token: String, id: Int): Mono<FileUrlResponse> {
        val fallbackResponse = FileUrlResponse(
            fileID = id,
            url = "https://http.cat/images/503.jpg"
        )
        return Mono.just(fallbackResponse)
    }

    override fun uploadImage(token: String, fileParts: Mono<FilePart>): Mono<UploadedFileResponse> {
        return Mono.error {
            ServiceUnavailableException(
                "Image upload service is currently unavailable."
            )
        }
    }

    override fun deleteImageById(token: String, id: Int): Mono<Void> {
        return Mono.error {
            ServiceUnavailableException(
                "Image upload service is currently unavailable."
            )
        }
    }

    override fun updateImage(token: String, id: Int, fileParts: Mono<FilePart>): Mono<UploadedFileResponse> {
        return Mono.error {
            ServiceUnavailableException(
                "Image update service is currently unavailable."
            )
        }
    }

}
