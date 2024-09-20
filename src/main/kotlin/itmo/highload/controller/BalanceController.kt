package itmo.highload.controller

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.service.BalanceService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/balances")
class BalanceController(val balanceService: BalanceService) {

    // TODO пагинация
    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(): List<BalanceResponse> {

    }

    @GetMapping("/{purposeId}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalancesByPurpose(@PathVariable purposeId: Int): List<BalanceResponse> {

    }

    @GetMapping("/purpose")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(): List<PurposeResponse> {

    }


}