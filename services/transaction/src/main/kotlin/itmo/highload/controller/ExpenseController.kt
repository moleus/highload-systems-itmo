package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.security.jwt.JwtUtils
import org.springframework.http.HttpStatus
import itmo.highload.service.TransactionService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/transactions/expenses")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class ExpenseController(
    private val transactionService: TransactionService,
    private val jwtUtils: JwtUtils,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Get all expenses",
        description = "Retrieve a list of all expenses, optionally filtered by purpose."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Expenses retrieved successfully",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getExpenses(
        @RequestParam(required = false) purposeId: Int?,
        @RequestHeader("Authorization") token: String
    ): Flux<TransactionResponse> = transactionService.getExpenses(purposeId, token)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Add an expense",
        description = "Submit a new expense transaction."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Expense successfully created",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addExpense(
        @RequestBody @Valid expenseDto: TransactionDto,
        @RequestHeader("Authorization") token: String,
    ): Mono<TransactionResponse> {
        val expenseManagerId = jwtUtils.extractUserId(token)
        return transactionService.addTransaction(expenseDto, expenseManagerId, isDonation = false)
    }
}
