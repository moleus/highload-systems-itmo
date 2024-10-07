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
@RequestMapping("\${app.base-url}/transactions/donations")
class DonationController(
    private val transactionService: TransactionService,
    private val jwtUtils: JwtUtils,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    fun getDonations(
        @RequestParam(required = false) purposeId: Int?, pageable: Pageable
    ): ResponseEntity<Flux<TransactionResponse>> {
        val donations = transactionService.getDonations(purposeId, pageable)
        return ResponseEntity.ok(donations)
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER')")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int, pageable: Pageable
    ): ResponseEntity<Flux<TransactionResponse>> {
        val donations = transactionService.getAllByUser(isDonation = true, userId = customerId, pageable = pageable)
        return ResponseEntity.ok(donations)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    fun addDonation(
        @RequestBody @Valid donationDto: TransactionDto,
        @RequestHeader("Authorization") token: String
    ): Mono<TransactionResponse> {
        val userId = jwtUtils.extractUserId(token)
        return transactionService.addTransaction(donationDto, userId, isDonation = true)
    }
}
