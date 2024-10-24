package itmo.highload.controller

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.model.BalanceMapper
import itmo.highload.service.BalanceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/transactions/balances")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class BalanceController(
    private val balanceService: BalanceService
) {

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getAllBalances(): Flux<BalanceResponse> {
        return balanceService.getAll().map { BalanceMapper.toBalanceResponse(it) }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun getBalanceById(@PathVariable id: Int): Mono<BalanceResponse> {
        return balanceService.getById(id).map { BalanceMapper.toBalanceResponse(it) }
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    fun getAllPurposes(): Flux<PurposeResponse> {
        return balanceService.getAllPurposes().map { BalanceMapper.toPurposeResponse(it) }
    }

    @PostMapping("/purposes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    fun addPurpose(@RequestBody @Valid purposeRequestDto: PurposeRequestDto): Mono<PurposeResponse> {
        return balanceService.addPurpose(purposeRequestDto.name).map { BalanceMapper.toPurposeResponse(it) }
    }
}
