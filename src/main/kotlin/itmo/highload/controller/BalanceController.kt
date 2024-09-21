@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.service.BalanceService
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/balances")
class BalanceController(val balanceService: BalanceService) {

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(pageable: Pageable): List<BalanceResponse> {
        return listOf()
    }

    @GetMapping("/{purposeId}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalancesByPurpose(
        @PathVariable purposeId: Int,
        pageable: Pageable
    ): List<BalanceResponse> {
        return listOf()
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(pageable: Pageable): List<PurposeResponse> {
        return listOf()
    }
}
