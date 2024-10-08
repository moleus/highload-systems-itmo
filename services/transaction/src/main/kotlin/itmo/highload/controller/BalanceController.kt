package itmo.highload.controller

import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.model.BalanceMapper
import itmo.highload.service.BalanceService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/balances")
class BalanceController(
    private val balanceService: BalanceService
) {

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(): Flux<BalanceResponse> {
        return balanceService.getAll()
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalanceById(@PathVariable id: Int): Mono<BalanceResponse> {
        return balanceService.getById(id).map { BalanceMapper.toBalanceResponse(it) }
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(): Flux<PurposeResponse> {
        return balanceService.getAllPurposes()
    }

    @PostMapping("/purposes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addPurpose(@RequestBody @NotBlank @Size(min = 1, max = 50) name: String): Mono<PurposeResponse> {
        return balanceService.addPurpose(name).map { BalanceMapper.toPurposeResponse(it) }
    }
}