package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.model.AnimalMapper
import itmo.highload.service.AnimalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/animals")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class AnimalController(val animalService: AnimalService) {
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all animals",
        description = "Retrieve a list of all animals with optional filters for name and adoption status.",

    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of animals retrieved", content = [
                Content(
                    schema = Schema(implementation = AnimalResponse::class)
                )
            ]),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) isNotAdopted: Boolean?,
        @RequestHeader("Authorization") token: String
    ): Flux<AnimalResponse> = animalService.getAll(name, isNotAdopted, token).map { AnimalMapper.toAnimalResponse(it) }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get animal by ID",
        description = "Retrieve information about a specific animal by its ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Animal details successfully retrieved",
                content = [Content(schema = Schema(implementation = AnimalResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAnimal(@PathVariable id: Int): Mono<AnimalResponse> {
        return animalService.getById(id).map { AnimalMapper.toAnimalResponse(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Add a new animal",
        description = "Create a new animal entry in the system."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Animal successfully added",
                content = [Content(schema = Schema(implementation = AnimalResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addAnimal(@RequestBody @Valid request: AnimalDto): Mono<AnimalResponse> {
        return animalService.save(request).map { AnimalMapper.toAnimalResponse(it) }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Update animal information",
        description = "Update the details of an existing animal."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Animal successfully updated",
                content = [Content(schema = Schema(implementation = AnimalResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun updateAnimal(
        @PathVariable id: Int, @RequestBody @Valid request: AnimalDto
    ): Mono<AnimalResponse> = animalService.update(id, request).map { AnimalMapper.toAnimalResponse(it) }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Delete an animal",
        description = "Remove an animal from the system."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Animal successfully deleted"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "404", description = "Animal not found"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun deleteAnimal(@PathVariable id: Int,
                     @RequestHeader("Authorization") token: String): Mono<Void> = animalService.delete(id, token)
}
