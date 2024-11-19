package itmo.highload.infrastructure.http

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.domain.mapper.BalanceMapper
import itmo.highload.domain.interactor.BalanceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("\${app.base-url}/balances")
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
    @Operation(
        summary = "Get all balances",
        description = "Retrieve a list of all balances."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Balances retrieved successfully",
                content = [Content(schema = Schema(implementation = BalanceResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAllBalances(): Flux<BalanceResponse> {
        return balanceService.getAll().map { BalanceMapper.toBalanceResponse(it) }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Get balance by ID",
        description = "Retrieve balance details by the specified ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Balance retrieved successfully",
                content = [Content(schema = Schema(implementation = BalanceResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Balance not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getBalanceById(@PathVariable id: Int): Mono<BalanceResponse> {
        return balanceService.getBalanceById(id).map { BalanceMapper.toBalanceResponse(it) }
    }

    @GetMapping("/purposes")
    @PreAuthorize("hasAnyAuthority('EXPENSE_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all purposes",
        description = "Retrieve a list of all purposes associated with balances."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Purposes retrieved successfully",
                content = [Content(schema = Schema(implementation = PurposeResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAllPurposes(): Flux<PurposeResponse> {
        return balanceService.getAllPurposes().map { BalanceMapper.toPurposeResponse(it) }
    }

    @PostMapping("/purposes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPENSE_MANAGER')")
    @Operation(
        summary = "Add a new purpose",
        description = "Create a new purpose entry associated with balances."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Purpose successfully added",
                content = [Content(schema = Schema(implementation = PurposeResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun addPurpose(@RequestBody @Valid purposeRequestDto: PurposeRequestDto): Mono<PurposeResponse> {
        return balanceService.addPurpose(purposeRequestDto.name).map { BalanceMapper.toPurposeResponse(it) }
    }
}
