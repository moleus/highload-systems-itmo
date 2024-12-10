package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.domain.entity.AdoptionRequestEntity
import itmo.highload.domain.interactor.AdoptionRequestInteractor
import itmo.highload.infrastructure.http.AdoptionRequestController
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class AdoptionRequestControllerTest {

    private val adoptionRequestService = mockk<AdoptionRequestInteractor>()
    private val jwtUtils = mockk<JwtUtils>()
    private val controller = AdoptionRequestController(adoptionRequestService, jwtUtils)

    @Test
    fun `getAll - should return adoption requests for manager`() {
        val token = "validToken"
        val adoptionRequests = listOf(
            AdoptionRequestEntity(
                id = 1,
                dateTime = LocalDateTime.now(),
                status = AdoptionStatus.PENDING,
                customerId = 1,
                managerId = 2,
                animalId = 101
            )
        )

        every { jwtUtils.extractUserId(token) } returns 1
        every { jwtUtils.extractRole(token) } returns Role.ADOPTION_MANAGER
        every { adoptionRequestService.getAll(null) } returns Flux.fromIterable(adoptionRequests)

        StepVerifier.create(controller.getAll(null, token))
            .expectNextMatches { it.id == 1 && it.status == AdoptionStatus.PENDING }
            .verifyComplete()
    }

    @Test
    fun `addAdoptionRequest - should save new adoption request`() {
        val token = "validToken"
        val userId = 1
        val animalId = 101
        val adoptionRequest = AdoptionRequestEntity(
            id = 1,
            dateTime = LocalDateTime.now(),
            status = AdoptionStatus.PENDING,
            customerId = userId,
            managerId = null,
            animalId = animalId
        )

        every { jwtUtils.extractUserId(token) } returns userId
        every { adoptionRequestService.save(userId, animalId) } returns Mono.just(adoptionRequest)

        StepVerifier.create(controller.addAdoptionRequest(animalId, token))
            .expectNextMatches {
                it.id == adoptionRequest.id &&
                        it.customerId == adoptionRequest.customerId &&
                        it.animalId == adoptionRequest.animalId &&
                        it.status == adoptionRequest.status
            }
            .verifyComplete()
    }

    @Test
    fun `deleteAdoptionRequest - should delete adoption request`() {
        val token = "validToken"
        val userId = 1
        val animalId = 101

        every { jwtUtils.extractUserId(token) } returns userId
        every { adoptionRequestService.delete(userId, animalId) } returns Mono.empty()

        StepVerifier.create(controller.deleteAdoptionRequest(animalId, token))
            .verifyComplete()
    }

    @Test
    fun `updateAdoptionRequest - should update status`() {
        val token = "validToken"
        val managerId = 2
        val updateDto = UpdateAdoptionRequestStatusDto(
            id = 1,
            status = AdoptionStatus.APPROVED
        )
        val adoptionRequest = AdoptionRequestEntity(
            id = 1,
            dateTime = LocalDateTime.now(),
            status = AdoptionStatus.APPROVED,
            customerId = 1,
            managerId = managerId,
            animalId = 101
        )

        every { jwtUtils.extractUserId(token) } returns managerId
        every { adoptionRequestService.update(managerId, updateDto) } returns Mono.just(adoptionRequest)

        StepVerifier.create(controller.updateAdoptionRequest(updateDto, token))
            .expectNextMatches {
                it.id == adoptionRequest.id &&
                        it.status == adoptionRequest.status &&
                        it.managerId == adoptionRequest.managerId
            }
            .verifyComplete()
    }
}
