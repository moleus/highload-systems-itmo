package itmo.highload.controller

import itmo.highload.service.OwnershipService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("\${app.base-url}/ownerships")
class OwnershipController(private val ownershipService: OwnershipService) {

    @GetMapping("/animals")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAdoptedAnimalsId(): Flux<Int> {
        return ownershipService.getAllAnimalsId()
    }
}
