package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.mapper.TransactionMapper
import itmo.highload.model.Transaction
import itmo.highload.model.User
import itmo.highload.model.enum.Role
import itmo.highload.service.TransactionService
import itmo.highload.utils.PaginationResponseHelper
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
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllDonations(
        @AuthenticationPrincipal user: User,
        pageable: Pageable
    ): Page<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = if (user.role == Role.EXPENSE_MANAGER) {
            transactionService.getAll(isDonation = true, limitedPageable)
        } else {
            transactionService.getAllByUser(isDonation = true, user.id, limitedPageable)
        }

        return mapPageToResponse(page)
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int,
        pageable: Pageable
    ): Page<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = transactionService.getAllByUser(isDonation = true, customerId, limitedPageable)
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
