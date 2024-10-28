package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.service.AnimalImageService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/animals/images")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class AnimalImageController(private val animalImageService: AnimalImageService) {

    @GetMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getImageByAnimalId(@PathVariable animalId: Int,
                           @RequestHeader("Authorization") token: String): Mono<FileUrlResponse> {
        return animalImageService.getImageByAnimalId(animalId, token)
    }

    @PostMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    fun addImageByAnimalId(@PathVariable animalId: Int,
                           @RequestHeader("Authorization") token: String) {
        animalImageService.saveImageByAnimalId(animalId, token)
    }

    @PutMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    fun updateImageByAnimalId(@PathVariable animalId: Int,
                              @RequestHeader("Authorization") token: String) {
        animalImageService.updateImageByAnimalId(animalId, token)
    }

    @DeleteMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    fun deleteImageByAnimalId(@PathVariable animalId: Int,
                              @RequestHeader("Authorization") token: String) {
        animalImageService.deleteAllByAnimalId(animalId, token)
    }


}