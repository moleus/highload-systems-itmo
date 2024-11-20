package itmo.highload.service

import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class AdoptionServiceFallbackTest {

    private val adoptionServiceFallback = AdoptionServiceFallback()

    @Test
    fun `should return empty flux if adoption service fails (fallback)`() {
        val token = "validToken"

        val emptyFlux = adoptionServiceFallback.getAllAdoptedAnimalsId(token)

        StepVerifier.create(emptyFlux)
            .verifyComplete()
    }
}
