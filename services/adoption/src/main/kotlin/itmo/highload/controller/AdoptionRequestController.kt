package itmo.highload.controller

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.AdoptionRequestService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/adoptions")
class AdoptionRequestController(
    private val adoptionRequestService: AdoptionRequestService,
    private val jwtUtils: JwtUtils,
) {
    private val logger = LoggerFactory.getLogger(AdoptionRequestController::class.java)

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAll(
        @RequestParam(required = false) status: AdoptionStatus?,
        @RequestHeader("Authorization") token: String,
        pageable: Pageable
    ): ResponseEntity<Flux<AdoptionRequestResponse>> {
        val userId = jwtUtils.extractUserId(token)
        val role = jwtUtils.extractRole(token)
        return ResponseEntity.ok(
            if (role == Role.ADOPTION_MANAGER) {
                adoptionRequestService.getAll(status, pageable)
            } else {
                adoptionRequestService.getAllByCustomer(userId, pageable)
            }
        )
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllStatuses(): ResponseEntity<Flux<AdoptionStatus>> {
        return ResponseEntity.ok(adoptionRequestService.getAllStatuses())
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(
        @PathVariable animalId: Int, @RequestHeader("Authorization") token: String
    ): ResponseEntity<Mono<AdoptionRequestResponse>> {
        val userId = jwtUtils.extractUserId(token)
        logger.error("User $userId is trying to adopt animal $animalId")
        return ResponseEntity.ok(adoptionRequestService.save(userId, animalId))
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto, @RequestHeader("Authorization") token: String
    ): ResponseEntity<Mono<AdoptionRequestResponse>> {
        val managerId = jwtUtils.extractUserId(token)
        return ResponseEntity.ok(adoptionRequestService.update(managerId, request))
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
