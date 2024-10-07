package itmo.highload.controller

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.service.TransactionService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/transactions/expenses")
class ExpenseController(
    private val transactionService: TransactionService,
    private val jwtUtils: JwtUtils,
) {

    @GetMapping
    fun getExpenses(
        @RequestParam(required = false) purposeId: Int?, pageable: Pageable
    ): ResponseEntity<Flux<TransactionResponse>> {
        val expenses = transactionService.getExpenses(purposeId, pageable)
        return ResponseEntity.ok(expenses)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    fun addExpense(
        @RequestBody @Valid expenseDto: TransactionDto,
        @RequestHeader("Authorization") token: String,
    ): Mono<TransactionResponse> {
        val expenseManagerId = jwtUtils.extractUserId(token)
        return transactionService.addTransaction(expenseDto, expenseManagerId, isDonation = false)
    }
}
