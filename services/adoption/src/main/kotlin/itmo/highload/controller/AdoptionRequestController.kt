package itmo.highload.controller

import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.AdoptionRequestMapper
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.AdoptionRequestService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/adoptions")
class AdoptionRequestController(
    private val adoptionRequestService: AdoptionRequestService,
    private val jwtUtils: JwtUtils,
) {
    @GetMapping
    fun getAll(
        @RequestParam(required = false) status: AdoptionStatus?,
        pageable: Pageable
    ): List<AdoptionRequestResponse> {
        return adoptionRequestService.getAll(status, pageable)
            .map { AdoptionRequestMapper.toResponse(it) }.content
    }

    @GetMapping("/statuses")
    fun getAllStatuses(): List<AdoptionStatus> {
        return adoptionRequestService.getAllStatuses()
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String
    ): AdoptionRequestResponse {
        val userId = jwtUtils.extractUserId(token)
        return AdoptionRequestMapper.toResponse(adoptionRequestService.save(userId, animalId))
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto,
        @RequestHeader("Authorization") token: String
    ): AdoptionRequestResponse {
        val managerId = jwtUtils.extractUserId(token)
        return AdoptionRequestMapper.toResponse(adoptionRequestService.update(managerId, request))
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int,
        @RequestHeader("Authorization") token: String
    ) {
        val customerId = jwtUtils.extractUserId(token)
        adoptionRequestService.delete(customerId, animalId)
    }
}
