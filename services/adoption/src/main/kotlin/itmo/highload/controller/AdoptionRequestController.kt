package itmo.highload.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.model.AdoptionRequestMapper
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.AdoptionRequestService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/adoptions")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class AdoptionRequestController(
    private val adoptionRequestService: AdoptionRequestService, private val jwtUtils: JwtUtils
) {
    private val logger = KotlinLogging.logger { }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all adoption requests",
        description = "Retrieve all adoption requests, filtered by status if specified. " +
                "Accessible to Adoption Managers and Customers."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of adoption requests",
                content = [Content(schema = Schema(implementation = AdoptionRequestResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAll(
        @RequestParam(required = false) status: AdoptionStatus?,
        @RequestHeader("Authorization") token: String,
    ): Flux<AdoptionRequestResponse> {
        val userId = jwtUtils.extractUserId(token)
        val role = jwtUtils.extractRole(token)
        logger.info { "User $userId with role $role requested all adoptions" }

        return if (role == Role.ADOPTION_MANAGER) {
            adoptionRequestService.getAll(status).map { AdoptionRequestMapper.toResponse(it) }
        } else {
            adoptionRequestService.getAllByCustomer(userId).map { AdoptionRequestMapper.toResponse(it) }
        }
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all adoption statuses",
        description = "Retrieve a list of all possible adoption statuses."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of adoption statuses",
                content = [Content(schema = Schema(implementation = AdoptionStatus::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAllStatuses(): Flux<AdoptionStatus> {
        return adoptionRequestService.getAllStatuses()
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(
        summary = "Request adoption of an animal",
        description = "Create a new adoption request for a specified animal."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Adoption request successfully created",
                content = [Content(schema = Schema(implementation = AdoptionRequestResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addAdoptionRequest(
        @PathVariable animalId: Int, @RequestHeader("Authorization") token: String
    ): Mono<AdoptionRequestResponse> {
        val userId = jwtUtils.extractUserId(token)
        logger.info { "Customer $userId requested adoption of animal $animalId" }
        return adoptionRequestService.save(userId, animalId).map { AdoptionRequestMapper.toResponse(it) }
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    @Operation(
        summary = "Update adoption request status",
        description = "Update the status of an adoption request."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Adoption request status successfully updated",
                content = [Content(schema = Schema(implementation = AdoptionRequestResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation"),
            ApiResponse(responseCode = "404", description = "Adoption request not found")
        ]
    )
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto, @RequestHeader("Authorization") token: String
    ): Mono<AdoptionRequestResponse> {
        val managerId = jwtUtils.extractUserId(token)
        return adoptionRequestService.update(managerId, request).map { AdoptionRequestMapper.toResponse(it) }
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(
        summary = "Delete adoption request",
        description = "Delete an existing adoption request for a specific animal."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Adoption request successfully deleted"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation"),
            ApiResponse(responseCode = "404", description = "Adoption request not found")
        ]
    )
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int, @RequestHeader("Authorization") token: String
    ): Mono<Void> {
        val customerId = jwtUtils.extractUserId(token)
        return adoptionRequestService.delete(customerId, animalId)
    }
}
