package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.service.AnimalImageService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
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
    @Operation(summary = "Get image by animal ID", description = "Retrieve the URL of the image associated with the specified animal.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Image URL retrieved successfully", content = [
                Content(schema = Schema(implementation = FileUrlResponse::class))
            ]),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal image not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getImageByAnimalId(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String
    ): Mono<FileUrlResponse> {
        return animalImageService.getImageByAnimalId(animalId, token)
    }

    @PostMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    @Operation(summary = "Add image for animal", description = "Upload a new image for the specified animal.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Image uploaded successfully", content = [
                Content(schema = Schema(implementation = UploadedFileResponse::class))
            ]),
            ApiResponse(responseCode = "400", description = "Invalid file format"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addImageByAnimalId(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "Image file to be uploaded", content = [Content(schema = Schema(type = "string",
            format = "binary"))])
        @RequestPart("file") imageData: FilePart
    ): Mono<UploadedFileResponse> {
        return animalImageService.saveImageByAnimalId(animalId, token, imageData)
    }

    @PutMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    @Operation(summary = "Update image for animal", description = "Replace the existing image for the specified animal with a new one.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Image updated successfully", content = [
                Content(schema = Schema(implementation = UploadedFileResponse::class))
            ]),
            ApiResponse(responseCode = "400", description = "Invalid file format"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal image not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun updateImageByAnimalId(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "New image file to be uploaded", content = [Content(schema = Schema(type = "string",
            format = "binary"))])
        @RequestPart("file") newFileData: FilePart
    ): Mono<UploadedFileResponse> {
        return animalImageService.updateImageByAnimalId(animalId, token, newFileData)
    }

    @DeleteMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    @Operation(summary = "Delete image for animal", description = "Delete the image associated with the specified animal.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal image not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun deleteImageByAnimalId(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String
    ):
            Mono<Void> = animalImageService.deleteAllByAnimalId(animalId, token)

}
