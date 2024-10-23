package itmo.highload.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
class AnimalController(val animalService: AnimalService) {
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all animals",
        description = "Retrieve a list of all animals with optional filters for name and adoption status.",
    )
    @SecurityRequirement(name = "bearer-key")
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
    fun getAnimal(@PathVariable id: Int): Mono<AnimalResponse> {
        return animalService.getById(id).map { AnimalMapper.toAnimalResponse(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun addAnimal(@RequestBody @Valid request: AnimalDto): Mono<AnimalResponse> {
        return animalService.save(request).map { AnimalMapper.toAnimalResponse(it) }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAnimal(
        @PathVariable id: Int, @RequestBody @Valid request: AnimalDto
    ): Mono<AnimalResponse> = animalService.update(id, request).map { AnimalMapper.toAnimalResponse(it) }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun deleteAnimal(@PathVariable id: Int): Mono<Void> = animalService.delete(id)
}
