package itmo.highload.service

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
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
    fun getImageUrlById(@RequestHeader("Authorization") token: String,
                        @PathVariable id: Int): Mono<FileUrlResponse>

    @PostMapping("/images/upload")
    fun uploadImage(@RequestHeader("Authorization") token: String,
                    fileParts: Mono<FilePart>
    ): Mono<UploadedFileResponse>

    @DeleteMapping("/images/{id}")
    fun deleteImageById(@RequestHeader("Authorization") token: String,
                        @PathVariable id: Int): Mono<Void>

}

@Component
class ImageServiceFallback : ImageService {

    override fun getImageUrlById(token: String, id: Int): Mono<FileUrlResponse> {
        val fallbackResponse = FileUrlResponse(
            fileID = id,
            url = "https://example.com/fallback-image.jpg"
        )
        return Mono.just(fallbackResponse)
    }

    override fun uploadImage(token: String, fileParts: Mono<FilePart>): Mono<UploadedFileResponse> {
        // TODO: тут нужно как-то бросать ошибку, чтобы принимающая сторона поняла, что сервис недоступен.
        // Сейчас в базу сохраняется -1
        val fallbackResponse = UploadedFileResponse(
            fileID = -1,
//            message = "Image upload service is currently unavailable."
        )
        return Mono.error { throw Error("Image upload service is currently unavailable.", Exception()) }
//        return Mono.just(fallbackResponse)
    }

    override fun deleteImageById(token: String, id: Int): Mono<Void> {
        println("Failed to delete image with ID $id. Image service is currently unavailable.")
        return Mono.empty()
    }

}