package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.mapper.TransactionMapper
import itmo.highload.model.Transaction
import itmo.highload.service.DEMO_EXPENSE_MANAGER_LOGIN
import itmo.highload.service.TransactionService
import itmo.highload.service.UserService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/transactions/expenses")
class ExpenseController(
    val transactionService: TransactionService,
    private val userService: UserService
) {

    private fun mapPageToResponse(page: Page<Transaction>): Page<TransactionResponse> {
        return page.map { transaction -> TransactionMapper.toResponse(transaction) }
    }

    @GetMapping
    fun getAllExpenses(pageable: Pageable): Page<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = transactionService.getAll(isDonation = false, limitedPageable)
        return mapPageToResponse(page)
    }

    @GetMapping("/{purposeId}")
    fun getExpensesByPurpose(
        @PathVariable purposeId: Int, pageable: Pageable
    ): Page<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = transactionService.getAllByPurpose(isDonation = false, purposeId, limitedPageable)
        return mapPageToResponse(page)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addExpense(
        @RequestBody @Valid expenseDto: TransactionDto,
    ): TransactionResponse {
        val demoExpenseManager = userService.getByLogin(DEMO_EXPENSE_MANAGER_LOGIN)
        val transaction = transactionService.addTransaction(expenseDto, demoExpenseManager, isDonation = false)
        return TransactionMapper.toResponse(transaction)
    }
}
