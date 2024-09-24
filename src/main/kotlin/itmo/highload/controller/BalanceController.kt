package itmo.highload.controller

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.mapper.BalanceMapper
import itmo.highload.model.Balance
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

    private fun mapPageToBalanceResponse(page: Page<Balance>): List<BalanceResponse> {
        return page.map { balance -> BalanceMapper.toBalanceResponse(balance) }.content
    }

    private fun mapPageToPurposeResponse(page: Page<Balance>): Page<PurposeResponse> {
        return page.map { balance -> BalanceMapper.toPurposeResponse(balance) }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(pageable: Pageable): List<BalanceResponse> {
        val page = balanceService.getAll(pageable)
        return mapPageToBalanceResponse(page)
    }

    @GetMapping("/{balanceId}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalanceById(@PathVariable balanceId: Int): BalanceResponse {
        val balance = balanceService.getById(balanceId)
        return BalanceMapper.toBalanceResponse(balance)
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(
        @RequestParam(required = false) hasHeaders: Boolean,
        pageable: Pageable
    ): ResponseEntity<List<PurposeResponse>> {
        val page = balanceService.getAll(pageable)

        if (hasHeaders) {
            return PaginationResponseHelper.createPaginatedResponseWithHeaders(mapPageToPurposeResponse(page))
        }

        return ResponseEntity.ok(mapPageToPurposeResponse(page).content)
    }

    @PostMapping("/purposes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun addPurpose(@RequestBody @NotBlank @Size(min = 1, max = 50) name: String): PurposeResponse {
        val purpose = balanceService.addPurpose(name)
        return BalanceMapper.toPurposeResponse(purpose)
    }
}
