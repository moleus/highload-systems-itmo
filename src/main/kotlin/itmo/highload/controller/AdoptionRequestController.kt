import itmo.highload.service.DEMO_ADOPTION_MANAGER_LOGIN
import itmo.highload.service.DEMO_CUSTOMER_LOGIN

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.mapper.AdoptionRequestMapper
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.service.AdoptionRequestService
import itmo.highload.service.UserService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/adoption-requests")
class AdoptionRequestController(
    val adoptionRequestService: AdoptionRequestService,
    private val userService: UserService
) {

    @GetMapping
    fun getAllAdoptionRequests(
        @RequestParam(required = false) status: AdoptionStatus, pageable: Pageable
    ): List<AdoptionRequestResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        return adoptionRequestService.getAll(status, limitedPageable).map { AdoptionRequestMapper.toResponse(it) }.content
    }

    @GetMapping("/statuses")
    fun getAllStatuses(): List<AdoptionStatus> {
        return adoptionRequestService.getAllStatuses()
    }

    @PostMapping("/{animalId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addAdoptionRequest(
        @PathVariable animalId: Int,
    ): AdoptionRequestResponse {
        val demoCustomer = userService.getByLogin(DEMO_CUSTOMER_LOGIN)
        return AdoptionRequestMapper.toResponse(adoptionRequestService.save(demoCustomer.id, animalId))
    }

    @PatchMapping
    fun updateAdoptionRequest(
        @RequestBody @Valid request: UpdateAdoptionRequestStatusDto,
    ): AdoptionRequestResponse {
        val demoManager = userService.getByLogin(DEMO_ADOPTION_MANAGER_LOGIN)
        return AdoptionRequestMapper.toResponse(adoptionRequestService.update(demoManager, request))
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdoptionRequest(
        @PathVariable animalId: Int
    ) {
        val demoCustomer = userService.getByLogin(DEMO_CUSTOMER_LOGIN)
        adoptionRequestService.delete(demoCustomer.id, animalId)
    }
}