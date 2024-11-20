package itmo.highload.infrastructure.http

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.domain.interactor.TransactionService
import itmo.highload.security.jwt.JwtUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/transactions/donations")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class DonationController(
    private val transactionService: TransactionService,
    private val jwtUtils: JwtUtils,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Get all donations",
        description = "Retrieve a list of all donations, optionally filtered by purpose."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Donations retrieved successfully",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getDonations(
        @RequestParam(required = false) purposeId: Int?,
        @RequestHeader("Authorization") token: String
    ): Flux<TransactionResponse> {
        val donations = transactionService.getDonations(purposeId, token)
        return donations
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Get donations by customer",
        description = "Retrieve donations made by a specific customer."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Customer's donations retrieved successfully",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Customer not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int,
        @RequestHeader("Authorization") token: String
    ): Flux<TransactionResponse> {
        val donations = transactionService.getAllByUser(isDonation = true, userId = customerId, token)
        return donations
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    @Operation(
        summary = "Add a donation",
        description = "Submit a donation transaction."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Donation successfully created",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addDonation(
        @RequestBody @Valid donationDto: TransactionDto,
        @RequestHeader("Authorization") token: String
    ): Mono<TransactionResponse> {
        val userId = jwtUtils.extractUserId(token)
        return transactionService.addTransaction(donationDto, userId, isDonation = true)
    }
}
