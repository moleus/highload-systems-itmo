package itmo.highload.service


import itmo.highload.api.dto.PurposeRequestDto
import itmo.highload.domain.interactor.BalanceServiceFallback
import itmo.highload.exceptions.ServiceUnavailableException
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class BalanceServiceFallbackTest {

    private val balanceServiceFallback = BalanceServiceFallback()

    @Test
    fun `getAllBalances should return error when fallback is triggered`() {
        val token = "validToken"

        val result = balanceServiceFallback.getAllBalances(token)

        StepVerifier.create(result)
            .expectErrorMatches { it is ServiceUnavailableException &&
                    it.message == "Balance service is currently unavailable." }
            .verify()
    }

    @Test
    fun `getBalanceById should return error when fallback is triggered`() {
        val token = "validToken"
        val id = 1

        val result = balanceServiceFallback.getBalanceById(token, id)

        StepVerifier.create(result)
            .expectErrorMatches { it is ServiceUnavailableException &&
                    it.message == "Balance service is currently unavailable." }
            .verify()
    }

    @Test
    fun `getAllPurposes should return error when fallback is triggered`() {
        val token = "validToken"

        val result = balanceServiceFallback.getAllPurposes(token)

        StepVerifier.create(result)
            .expectErrorMatches { it is ServiceUnavailableException &&
                    it.message == "Balance service is currently unavailable." }
            .verify()
    }

    @Test
    fun `addPurpose should return error when fallback is triggered`() {
        val token = "validToken"
        val purposeRequestDto = PurposeRequestDto(
            name = "Test Purpose",
        )

        val result = balanceServiceFallback.addPurpose(token, purposeRequestDto)

        StepVerifier.create(result)
            .expectErrorMatches { it is ServiceUnavailableException &&
                    it.message == "Balance service is currently unavailable." }
            .verify()
    }
}
