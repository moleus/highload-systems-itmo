package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.model.ImageRef
import itmo.highload.service.ImagesService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/images")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class ImagesController(
    private val imagesService: ImagesService
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADOPTION_MANAGER')")
    @Operation(
        summary = "Get image by ID",
        description = "Retrieve image details by the specified ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Image retrieved successfully",
                content = [Content(schema = Schema(implementation = ImageRef::class))]
            ),
            ApiResponse(responseCode = "404", description = "Image not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getImageById(@PathVariable id: Int): Mono<ImageRef> {
        return imagesService.getImageById(id)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADOPTION_MANAGER')")
    @Operation(
        summary = "Upload a new image",
        description = "Upload a new image and save its reference."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Image successfully uploaded",
                content = [Content(schema = Schema(implementation = ImageRef::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun uploadImage(@RequestParam("file") uploadedFile: MultipartFile): Mono<ImageRef> {
        return imagesService.saveImage(uploadedFile)
    }
}
