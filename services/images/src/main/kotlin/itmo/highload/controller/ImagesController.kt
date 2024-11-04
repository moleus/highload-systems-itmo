package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.service.ImagesService
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/images")
@OpenAPIDefinition(
    servers = [Server(url = "http://localhost:8080")]
)
class ImagesController(
    private val imagesService: ImagesService
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADOPTION_MANAGER')")
    @Operation(
        summary = "Get image by ID", description = "Retrieve image details by the specified ID."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Image retrieved successfully",
            content = [Content(schema = Schema(implementation = FileUrlResponse::class))]
        ), ApiResponse(responseCode = "404", description = "Image not found"), ApiResponse(
            responseCode = "401", description = "Unauthorized request"
        ), ApiResponse(responseCode = "403", description = "No authority for this operation")]
    )
    fun getImageUrlById(@PathVariable id: Int) = imagesService.getImageById(id).map { FileUrlResponse(it.id, it.url) }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADOPTION_MANAGER')")
    @Operation(
        summary = "Upload a new image", description = "Upload a new image and save its reference."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "201",
            description = "Image successfully uploaded",
            content = [Content(schema = Schema(implementation = UploadedFileResponse::class))]
        ), ApiResponse(
            responseCode = "400", description = "Invalid request parameters"
        ), ApiResponse(responseCode = "401", description = "Unauthorized request"), ApiResponse(
            responseCode = "403", description = "No authority for this operation"
        )]
    )
    fun uploadImage(@RequestPart("file") fileParts: Mono<FilePart>) =
        fileParts.flatMap { part -> imagesService.saveImage(part) }.map { UploadedFileResponse(fileID = it.id) }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Delete image by ID", description = "Delete image by the specified ID."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "Image deleted successfully"
        ), ApiResponse(responseCode = "404", description = "Image not found"), ApiResponse(
            responseCode = "401", description = "Unauthorized request"
        ), ApiResponse(responseCode = "403", description = "No authority for this operation")]
    )
    fun deleteImageById(@PathVariable id: Int) = imagesService.deleteImageById(id)

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Update image by ID",
        description = "Update an existing image with a new file by the specified ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Image updated successfully", content = [Content(schema = Schema(implementation = FileUrlResponse::class))]),
            ApiResponse(responseCode = "404", description = "Image not found"),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun updateImageById(@PathVariable id: Int, @RequestPart("file") filePart: Mono<FilePart>): Mono<FileUrlResponse> {
        return filePart.flatMap { part ->
            imagesService.updateImageById(id, part)
        }.map { updatedImage ->
            FileUrlResponse(fileID = updatedImage.id, url = updatedImage.url)
        }
    }
}
