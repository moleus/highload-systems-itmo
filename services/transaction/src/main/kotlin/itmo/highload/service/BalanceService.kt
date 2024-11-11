package itmo.highload.service

import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.exceptions.ServiceUnavailableException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ReactiveFeignClient(
    value = "balance-service",
    url = "http://\${services.endpoints.balances:localhost:8099}/api/v1",
    fallback = BalanceServiceFallback::class
)

interface BalanceService {
    @GetMapping("/balances")
    fun getAllBalances(
        @RequestHeader("Authorization") token: String
    ): Flux<BalanceResponse>

    @GetMapping("/balances/{id}")
    fun getBalanceById(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: Int
    ): Mono<BalanceResponse>

    @GetMapping("/balances/purposes")
    fun getAllPurposes(
        @RequestHeader("Authorization") token: String,
    ): Flux<PurposeResponse>

    @PostMapping("/balances/purposes")
    fun addPurpose(
        @RequestHeader("Authorization") token: String,
        @RequestBody purposeRequestDto: PurposeRequestDto
    ): Mono<PurposeResponse>

}

@Component
class BalanceServiceFallback : BalanceService {

    override fun getAllBalances(token: String): Flux<BalanceResponse> {
        return Flux.error {
            ServiceUnavailableException(
                "Balance service is currently unavailable."
            )
        }
    }

    override fun getBalanceById(token: String, id: Int): Mono<BalanceResponse> {
        return Mono.error {
            ServiceUnavailableException(
                "Balance service is currently unavailable."
            )
        }
    }

    override fun getAllPurposes(token: String): Flux<PurposeResponse> {
        return Flux.error {
            ServiceUnavailableException(
                "Balance service is currently unavailable."
            )
        }
    }

    override fun addPurpose(token: String, purposeRequestDto: PurposeRequestDto): Mono<PurposeResponse> {
        return Mono.error {
            ServiceUnavailableException(
                "Balance service is currently unavailable."
            )
        }
    }

}
