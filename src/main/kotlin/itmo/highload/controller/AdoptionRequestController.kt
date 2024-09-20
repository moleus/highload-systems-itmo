package itmo.highload.controller

import itmo.highload.dto.AdoptionRequestDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.service.AdoptionRequestService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/adoption-requests")
class AdoptionRequestController(val adoptionRequestService: AdoptionRequestService) {

    // TODO пагинация
    @GetMapping
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllAdoptionRequests(): List<AdoptionRequestResponse> {

    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getPendingAdoptionRequests(): List<AdoptionRequestResponse> {

    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAdoptionRequestsByCustomerForManager(@PathVariable customerId: Int): List<AdoptionRequestResponse> {

    }

    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun getAdoptionRequestsByCustomerForCustomer(): List<AdoptionRequestResponse> {
        //customerId берется из securityContext
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addAdoptionRequest(@PathVariable animalId: Int): AdoptionRequestResponse {

    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAdoptionRequest(@RequestBody @Valid request: AdoptionRequestDto): AdoptionRequestResponse {

    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun deleteAdoptionRequest(@PathVariable animalId: Int) {
        // только если статус PENDING
    }
}