package itmo.highload.service

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux

@ReactiveFeignClient(
    value = "image-service",
    // TODO поменять на верный
    url = "http://localhost:8085/api/v1",
    fallback = ImageServiceFallback::class
)
interface ImageService {
    @GetMapping("/images/{id}")
    fun getImageUrlById(@RequestHeader("Authorization") token: String,
                               @PathVariable id: Int): FileUrlResponse

    @PostMapping("/upload")
    fun uploadImage(@RequestHeader("Authorization") token: String): Flux<Int>

    @DeleteMapping("/{id}")
    fun deleteImageById(@RequestHeader("Authorization") token: String,
                               @PathVariable id: Int): Flux<Int>

}

@Component
class ImageServiceFallback : ImageService {
    override fun getImageUrlById(token: String, id: Int): Flux<Int> {
        TODO("Not yet implemented")
    }

    override fun uploadImage(token: String): Flux<Int> {
        TODO("Not yet implemented")
    }

    override fun deleteImageById(token: String, id: Int): Flux<Int> {
        TODO("Not yet implemented")
    }

}