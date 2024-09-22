package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllDonations(
        @AuthenticationPrincipal user: User,
        pageable: Pageable
    ): Page<TransactionResponse> {
        if (user.role == UserRole.EXPENSE_MANAGER) {
            return transactionService.getAll(isDonation = true, pageable)
        }
        return transactionService.getAllByUser(isDonation = true, user.id, pageable)
    }

    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int,
        pageable: Pageable
    ): Page<TransactionResponse> {
        return transactionService.getAllByUser(isDonation = true, customerId, pageable)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addDonation(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid donationDto: TransactionDto
    ): TransactionResponse {
        return transactionService.addTransaction(donationDto, user, isDonation = true)
    }
}
