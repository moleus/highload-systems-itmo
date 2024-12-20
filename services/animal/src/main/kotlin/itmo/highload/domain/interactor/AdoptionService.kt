package itmo.highload.domain.interactor

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux

@ReactiveFeignClient(
    value = "adoption-service",
    url = "http://\${services.endpoints.adoption:localhost:8085}/api/v1",
    fallback = AdoptionServiceFallback::class
)
interface AdoptionService {
    @GetMapping("/ownerships/animals")
    fun getAllAdoptedAnimalsId(@RequestHeader("Authorization") token: String): Flux<Int>

}

@Component
class AdoptionServiceFallback : AdoptionService {
    override fun getAllAdoptedAnimalsId(@RequestHeader("Authorization") token: String): Flux<Int> {
        return Flux.empty()
    }
}
