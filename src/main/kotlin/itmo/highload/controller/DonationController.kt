package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.mapper.TransactionMapper
import itmo.highload.model.Transaction
import itmo.highload.model.User
import itmo.highload.model.enum.UserRole
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
@RequestMapping("/api/v1/transactions/donations")
class DonationController(val transactionService: TransactionService) {

    private fun mapPageToResponse(page: Page<Transaction>): Page<TransactionResponse> {
        return page.map { transaction -> TransactionMapper.toResponse(transaction) }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllDonations(
        @AuthenticationPrincipal user: User,
        pageable: Pageable
    ): Page<TransactionResponse> {
        val page = if (user.role == UserRole.EXPENSE_MANAGER) {
            transactionService.getAll(isDonation = true, pageable)
        } else {
            transactionService.getAllByUser(isDonation = true, user.id, pageable)
        }

        return mapPageToResponse(page)
    }

    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int,
        pageable: Pageable
    ): Page<TransactionResponse> {
        val page = transactionService.getAllByUser(isDonation = true, customerId, pageable)
        return mapPageToResponse(page)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addDonation(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid donationDto: TransactionDto
    ): TransactionResponse {
        val transaction = transactionService.addTransaction(donationDto, user, isDonation = true)
        return TransactionMapper.toResponse(transaction)
    }
}
