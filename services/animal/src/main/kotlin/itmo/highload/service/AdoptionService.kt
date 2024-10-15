package itmo.highload.service

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(name = "adoption-service", url = "http://localhost:8085", fallback = AdoptionServiceFallback::class)
interface AdoptionService {
    @GetMapping("/ownerships/animals")
    fun getAllAdoptedAnimalsId(): List<Int>

}

@Component
class AdoptionServiceFallback : AdoptionService {
    override fun getAllAdoptedAnimalsId(): List<Int> {
        return mutableListOf()
    }
}
