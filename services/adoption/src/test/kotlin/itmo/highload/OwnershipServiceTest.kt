package itmo.highload

import io.mockk.every
import io.mockk.mockk
import itmo.highload.model.Ownership
import itmo.highload.repository.OwnershipRepository
import itmo.highload.service.OwnershipService
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class OwnershipServiceTest {

    private val ownershipRepository: OwnershipRepository = mockk()
    private val ownershipService = OwnershipService(ownershipRepository)

    @Test
    fun `getAllAnimalsId - should return list of animal IDs`() {
        val ownerships = listOf(
            Ownership(customerId = 1, animalId = 101),
            Ownership(customerId = 2, animalId = 102)
        )
        every { ownershipRepository.findAll() } returns ownerships

        val result = ownershipService.getAllAnimalsId()

        StepVerifier.create(result)
            .expectNext(101)
            .expectNext(102)
            .verifyComplete()
    }
}

