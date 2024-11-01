package itmo.highload.service

import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import reactivefeign.spring.config.ReactiveFeignClient

@ReactiveFeignClient(
    value = "image-service",
    // TODO поменять на верный
    url = "\${services.endpoints.images:http://localhost:8085/api/v1}",
    fallback = ImageServiceFallback::class
)
interface ImageService {
    @GetMapping("/images/{id}")
    fun getImageUrlById(@RequestHeader("Authorization") token: String,
                        @PathVariable id: Int): FileUrlResponse

    @PostMapping("/images/upload")
    fun uploadImage(@RequestHeader("Authorization") token: String): UploadedFileResponse

    @DeleteMapping("/images/{id}")
    fun deleteImageById(@RequestHeader("Authorization") token: String,
                        @PathVariable id: Int)

}

@Component
class ImageServiceFallback : ImageService {
    override fun getImageUrlById(token: String, id: Int): FileUrlResponse {
        TODO("Not yet implemented")
    }

    override fun uploadImage(token: String): UploadedFileResponse {
        TODO("Not yet implemented")
    }

    override fun deleteImageById(token: String, id: Int) {
        TODO("Not yet implemented")
    }

}