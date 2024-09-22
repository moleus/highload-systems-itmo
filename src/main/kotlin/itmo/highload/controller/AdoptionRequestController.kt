@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.service.AdoptionRequestService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/adoption-requests")
class AdoptionRequestController(val adoptionRequestService: AdoptionRequestService) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAdoptionRequests(
        /*@RequestParam(required = false) status: AdoptionRequestStatus,
        @AuthenticationPrincipal user: User,*/
        pageable: Pageable
    ): List<AdoptionRequestResponse> {
        /*
        if (user.role == Role.ADOPTION_MANAGER) {
            adoptionRequestService.getAll(status, pageable)
        } else if (user.role == Role.CUSTOMER) {
            adoptionRequestService.getAllByCustomer(user.id, pageable)
        }
         */

        return listOf()
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllStatuses(
        pageable: Pageable
    )/*: List<AdoptionRequestStatus>*/ {
        /*
            adoptionRequestService.getAllStatuses()
         */
    }

    @PostMapping("/{animalId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(
        @PathVariable animalId: Int,
        /*@AuthenticationPrincipal customer: User*/
    ): AdoptionRequestResponse {
        return adoptionRequestService.save(/*customer.id,*/ animalId)
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto,
        /*@AuthenticationPrincipal manager: User*/
    ): AdoptionRequestResponse {
        return adoptionRequestService.update(/*manager.id,*/ request)
    }

    @DeleteMapping("/{animalId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int,
        /*@AuthenticationPrincipal customer: User*/
    ) {
        adoptionRequestService.delete(/*customer.id,*/ animalId)
    }
}
