package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.model.User
import itmo.highload.service.TransactionService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/transactions/expenses")
class ExpenseController(val transactionService: TransactionService) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllExpenses(pageable: Pageable): Page<TransactionResponse> {
        return transactionService.getAll(isDonation = false, pageable)
    }

    @GetMapping("/{purposeId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getExpensesByPurpose(
        @PathVariable purposeId: Int,
        pageable: Pageable
    ): Page<TransactionResponse> {
        return transactionService.getAllByPurpose(isDonation = false, purposeId, pageable)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun addExpense(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid expenseDto: TransactionDto
    ): TransactionResponse {
        return transactionService.addTransaction(expenseDto, user, isDonation = false)
    }
}
