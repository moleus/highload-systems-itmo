package itmo.highload

import io.mockk.every
import io.mockk.mockk
import itmo.highload.controller.OwnershipController
import itmo.highload.service.OwnershipService
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class OwnershipControllerTest {

    private val ownershipService = mockk<OwnershipService>()
    private val controller = OwnershipController(ownershipService)

    @Test
    fun `getAllAdoptedAnimalsId - should return list of animal IDs`() {
        val animalIds = listOf(101, 102, 103)

        every { ownershipService.getAllAnimalsId() } returns Flux.fromIterable(animalIds)

        StepVerifier.create(controller.getAllAdoptedAnimalsId())
            .expectNext(101, 102, 103)
            .verifyComplete()
    }
}
