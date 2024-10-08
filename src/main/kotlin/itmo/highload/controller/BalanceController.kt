package itmo.highload.controller

import itmo.highload.dto.PurposeDto
import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.mapper.BalanceMapper
import itmo.highload.model.Balance
import itmo.highload.service.BalanceService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/balances")
class BalanceController(val balanceService: BalanceService) {

    private fun mapPageToBalanceResponse(page: Page<Balance>): List<BalanceResponse> {
        return page.map { balance -> BalanceMapper.toBalanceResponse(balance) }.content
    }

    private fun mapPageToPurposeResponse(page: Page<Balance>): Page<PurposeResponse> {
        return page.map { balance -> BalanceMapper.toPurposeResponse(balance) }
    }

    @GetMapping
    fun getAllBalances(pageable: Pageable): List<BalanceResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = balanceService.getAll(limitedPageable)
        return mapPageToBalanceResponse(page)
    }

    @GetMapping("/{id}")
    fun getBalanceById(@PathVariable id: Int): BalanceResponse {
        val balance = balanceService.getById(id)
        return BalanceMapper.toBalanceResponse(balance)
    }

    @GetMapping("/purposes")
    fun getAllPurposes(
        @RequestParam(required = false) hasHeaders: Boolean, pageable: Pageable
    ): ResponseEntity<List<PurposeResponse>> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        val page = balanceService.getAll(limitedPageable)

        if (hasHeaders) {
            return PaginationResponseHelper.createPaginatedResponseWithHeaders(mapPageToPurposeResponse(page))
        }

        return ResponseEntity.ok(mapPageToPurposeResponse(page).content)
    }

    @PostMapping("/purposes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addPurpose(@RequestBody @Valid purposeDto: PurposeDto): PurposeResponse {
        val purpose = balanceService.addPurpose(purposeDto.name)
        return BalanceMapper.toPurposeResponse(purpose)
    }
}
