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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/transactions/expenses")
class ExpenseController(
    val transactionService: TransactionService,
    private val userService: UserService
) {

    private fun mapPageToResponse(page: Page<Transaction>): List<TransactionResponse> {
        return page.map { transaction -> TransactionMapper.toResponse(transaction) }.content
    }

    @GetMapping
    fun getExpenses(
        @RequestParam(required = false) purposeId: Int?, pageable: Pageable
    ): List<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        return mapPageToResponse(transactionService.getExpenses(purposeId, limitedPageable))
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
