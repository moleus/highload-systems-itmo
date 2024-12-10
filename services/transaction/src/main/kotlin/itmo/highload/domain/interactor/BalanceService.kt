package itmo.highload.domain.interactor

import io.github.oshai.kotlinlogging.KotlinLogging
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

    companion object {
        private const val BALANCE_SERVICE_IS_CURRENTLY_UNAVAILABLE = "Balance service is currently unavailable."
    }
    private val logger = KotlinLogging.logger {}

    override fun getAllBalances(token: String): Flux<BalanceResponse> {
        logger.warn { "Fallback triggered for getAllBalances" }
        return Flux.error {
            ServiceUnavailableException(BALANCE_SERVICE_IS_CURRENTLY_UNAVAILABLE)
        }
    }

    override fun getBalanceById(token: String, id: Int): Mono<BalanceResponse> {
        logger.warn { "${"Fallback triggered for getBalanceById, id: {}"} $id" }
        return Mono.error {
            ServiceUnavailableException(BALANCE_SERVICE_IS_CURRENTLY_UNAVAILABLE)
        }
    }

    override fun getAllPurposes(token: String): Flux<PurposeResponse> {
        logger.warn { "Fallback triggered for getAllPurposes" }
        return Flux.error {
            ServiceUnavailableException(BALANCE_SERVICE_IS_CURRENTLY_UNAVAILABLE)
        }
    }

    override fun addPurpose(token: String, purposeRequestDto: PurposeRequestDto): Mono<PurposeResponse> {
        logger.warn { "${"Fallback triggered for addPurpose, purposeRequestDto: {}"} $purposeRequestDto" }
        return Mono.error {
            ServiceUnavailableException(BALANCE_SERVICE_IS_CURRENTLY_UNAVAILABLE)
        }
    }

}
