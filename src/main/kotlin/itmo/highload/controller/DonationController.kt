package itmo.highload.controller

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.mapper.TransactionMapper
import itmo.highload.model.Transaction
import itmo.highload.service.DEMO_CUSTOMER_LOGIN
import itmo.highload.service.TransactionService
import itmo.highload.service.UserService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/transactions/donations")
class DonationController(
    val transactionService: TransactionService, private val userService: UserService
) {

    private fun mapPageToResponse(page: Page<Transaction>): List<TransactionResponse> {
        return page.map { transaction -> TransactionMapper.toResponse(transaction) }.content
    }

    @GetMapping
    fun getDonations(
        @RequestParam(required = false) purposeId: Int?, pageable: Pageable
    ): List<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        return mapPageToResponse(transactionService.getDonations(purposeId, limitedPageable))
    }

    @GetMapping("/{customerId}")
    fun getDonationsByCustomerForManager(
        @PathVariable customerId: Int, pageable: Pageable
    ): List<TransactionResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = transactionService.getAllByUser(isDonation = true, customerId, limitedPageable)
        return mapPageToResponse(page)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addDonation(
        @RequestBody @Valid donationDto: TransactionDto
    ): TransactionResponse {
        val demoCustomer = userService.getByLogin(DEMO_CUSTOMER_LOGIN)
        val transaction = transactionService.addTransaction(donationDto, demoCustomer, isDonation = true)
        return TransactionMapper.toResponse(transaction)
    }
}
