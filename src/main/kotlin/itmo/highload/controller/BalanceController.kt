@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.service.BalanceService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/balances")
class BalanceController(val balanceService: BalanceService) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(pageable: Pageable): Page<BalanceResponse> {
        return balanceService.getAll(pageable)
    }

    @GetMapping("/{balanceId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalanceById(@PathVariable balanceId: Int): BalanceResponse {
        return balanceService.getById(balanceId)
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(
        @RequestParam(required = false) hasHeaders: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<PurposeResponse>> {
        val page: Page<PurposeResponse> = balanceService.getAllPurposes(pageable)
        if (hasHeaders) {
            return PaginationResponseHelper.createPaginatedResponse(page)
        }
        return ResponseEntity.ok(page)
    }

    @PostMapping("/purposes")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun addPurpose(@RequestBody @NotBlank @Size(min = 1, max = 50) name: String): PurposeResponse {
        return balanceService.addPurpose(name)
    }
}
