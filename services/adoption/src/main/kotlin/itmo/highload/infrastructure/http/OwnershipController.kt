package itmo.highload.infrastructure.http

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.servers.Server
import itmo.highload.domain.interactor.OwnershipInteractor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("\${app.base-url}/ownerships")
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8080")
    ]
)
class OwnershipController(private val ownershipService: OwnershipInteractor) {

    @GetMapping("/animals")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    @Operation(
        summary = "Get all adopted animal IDs",
        description = "Retrieve a list of IDs of all animals that have been adopted."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of adopted animal IDs",
                content = [Content(schema = Schema(implementation = Int::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized request"),
            ApiResponse(responseCode = "403", description = "No authority for this operation")
        ]
    )
    fun getAllAdoptedAnimalsId(): Flux<Int> {
        return ownershipService.getAllAnimalsId()
    }
}
