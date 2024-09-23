@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.model.User
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.UserRole
import itmo.highload.service.AdoptionRequestService
import itmo.highload.service.mapper.AdoptionRequestMapper
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/api/v1/adoption-requests")
class AdoptionRequestController(val adoptionRequestService: AdoptionRequestService) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAdoptionRequests(
        @RequestParam(required = false) status: AdoptionStatus,
        @AuthenticationPrincipal user: User,
        pageable: Pageable
    ): List<AdoptionRequestResponse> {
        require(user.role == UserRole.ADOPTION_MANAGER || user.role == UserRole.CUSTOMER) {
            "Invalid user role: ${user.role}"
        }
        return if (user.role == UserRole.ADOPTION_MANAGER) {
            adoptionRequestService.getAll(status, pageable)
                .map { AdoptionRequestMapper.toResponse(it) }
                .content
        } else {
            adoptionRequestService.getAllByCustomer(user.id, pageable)
                .map { AdoptionRequestMapper.toResponse(it) }
                .content
        }
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllStatuses(
        pageable: Pageable
    ): List<AdoptionStatus> {
        return adoptionRequestService.getAllStatuses()
    }

    @PostMapping("/{animalId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(
        @PathVariable animalId: Int,
        @AuthenticationPrincipal customer: User
    ): AdoptionRequestResponse {
        return AdoptionRequestMapper.toResponse(adoptionRequestService.save(customer.id, animalId))
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto,
        @AuthenticationPrincipal manager: User
    ): AdoptionRequestResponse {
        return AdoptionRequestMapper.toResponse(adoptionRequestService.update(manager, request))
    }

    @DeleteMapping("/{animalId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int,
        @AuthenticationPrincipal customer: User
    ) {
        adoptionRequestService.delete(customer.id, animalId)
    }
}
