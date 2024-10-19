package itmo.highload.controller

import io.github.oshai.kotlinlogging.KotlinLogging
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
class AdoptionRequestController(
    private val adoptionRequestService: AdoptionRequestService,
    private val jwtUtils: JwtUtils
) {
    private val logger = KotlinLogging.logger { }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAll(
        @RequestParam(required = false) status: AdoptionStatus?,
        @RequestHeader("Authorization") token: String,
    ): Flux<AdoptionRequestResponse> {
        val userId = jwtUtils.extractUserId(token)
        val role = jwtUtils.extractRole(token)
        logger.info { "User $userId is trying to get adoption requests" }

        return if (role == Role.ADOPTION_MANAGER) {
            adoptionRequestService.getAll(status).map { AdoptionRequestMapper.toResponse(it) }
        } else {
            adoptionRequestService.getAllByCustomer(userId).map { AdoptionRequestMapper.toResponse(it) }
        }
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllStatuses(): Flux<AdoptionStatus> {
        return adoptionRequestService.getAllStatuses()
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(
        @PathVariable animalId: Int, @RequestHeader("Authorization") token: String
    ): Mono<AdoptionRequestResponse> {
        val userId = jwtUtils.extractUserId(token)
        logger.info {"User $userId is trying to adopt animal $animalId" }
        return adoptionRequestService.save(userId, animalId).map { AdoptionRequestMapper.toResponse(it) }
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto, @RequestHeader("Authorization") token: String
        ): Mono<AdoptionRequestResponse> {
        val managerId = jwtUtils.extractUserId(token)
            return adoptionRequestService.update(managerId, request).map { AdoptionRequestMapper.toResponse(it) }
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int, @RequestHeader("Authorization") token: String
    ): Mono<Void> {
        val customerId = jwtUtils.extractUserId(token)
        return adoptionRequestService.delete(customerId, animalId)
    }
}
