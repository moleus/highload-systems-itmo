@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.service.TransactionService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllDonations(
        @AuthenticationPrincipal user: UserDetails,
        pageable: Pageable
    ): List<TransactionResponse> {
        return listOf()
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int,
        pageable: Pageable
    ): List<TransactionResponse> {
        return listOf()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    fun addDonation(@RequestBody @Valid donationDto: TransactionDto): TransactionResponse {
        return transactionService.saveDonation(donationDto)
    }
}
