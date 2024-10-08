package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.exceptions.InvalidAdoptionRequestStatusException
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.Ownership
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.OwnershipRepository
import itmo.highload.service.AdoptionRequestService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.kotlin.test.test
import java.time.LocalDateTime
import java.util.*

class AdoptionRequestServiceTest {

    private val adoptionRequestRepository: AdoptionRequestRepository = mockk()
    private val ownershipRepository: OwnershipRepository = mockk()
    private val adoptionRequestService = AdoptionRequestService(
        adoptionRequestRepository, ownershipRepository
    )

    @Test
    fun `should save adoption request if no existing request found`() {

        val adoptionRequest = AdoptionRequest(
            dateTime = LocalDateTime.now(), customerId = 1, animalId = 1, status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns Optional.empty()
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest

        adoptionRequestService.save(1, 1).subscribe {
            assertEquals(adoptionRequest, it)
            verify { adoptionRequestRepository.save(any()) }
        }
    }

    @Test
    fun `should throw EntityAlreadyExistsException if adoption request already exists`() {
        val customerId = 1
        val animalId = 1

        val existingRequest = AdoptionRequest(
            dateTime = LocalDateTime.now(),
            customerId = customerId,
            animalId = animalId,
            status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns Optional.of(existingRequest)

        adoptionRequestService.save(1, 1)
            .test()
            .expectErrorMessage("An adoption request already exists for customer ID: 1 and animal ID: 1")
            .verify()
    }

    @Test
    fun `should update adoption request status to DENIED without creating ownership`() {
        val customerId = 1
        val animalId = 1
        val managerId = 1

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customerId = customerId,
            animalId = animalId,
            status = AdoptionStatus.PENDING
        )
        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.DENIED)

        every { adoptionRequestRepository.findById(1) } returns Optional.of(adoptionRequest)
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest

        adoptionRequestService.update(managerId, requestDto)
            .test()
            .assertNext {
                assertEquals(AdoptionStatus.DENIED, it.status)
                assertEquals(managerId, it.managerId)
            }
            .verifyComplete()
        verify(exactly = 0) { ownershipRepository.save(any()) }
    }

    @Test
    fun `should create ownership when adoption request is approved`() {

        val adoptionRequest = AdoptionRequest(
            id = 1, dateTime = LocalDateTime.now(), customerId = 1, animalId = 1, status = AdoptionStatus.PENDING
        )
        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.APPROVED)

        every { adoptionRequestRepository.findById(1) } returns Optional.of(adoptionRequest)
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest
        every { ownershipRepository.save(any()) } returns Ownership(1, 1)

        adoptionRequestService.update(1, requestDto).subscribe { result ->
            assertEquals(AdoptionStatus.APPROVED, result.status)
            verify { ownershipRepository.save(any()) }
            verify { adoptionRequestRepository.save(any()) }
        }
    }

    @Test
    fun `should throw EntityNotFoundException when adoption request is not found`() {

        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.APPROVED)

        every { adoptionRequestRepository.findById(1) } returns Optional.empty()

        adoptionRequestService.update(1, requestDto)
            .test()
            .verifyErrorMatches {
                it is EntityNotFoundException && it.message == "Adoption request not found"
            }

        verify(exactly = 0) { ownershipRepository.save(any()) }
        verify(exactly = 0) { adoptionRequestRepository.save(any()) }
    }

    @Test
    fun `should delete adoption request if status is PENDING`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customerId = 1,
            animalId = 1,
            status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns Optional.of(adoptionRequest)
        every { adoptionRequestRepository.delete(adoptionRequest) } returns Unit

        adoptionRequestService.delete(1, 1)
            .test()
            .verifyComplete()

        verify { adoptionRequestRepository.delete(adoptionRequest) }
    }

    @Test
    fun `should throw EntityNotFoundException if adoption request is not found`() {
        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns Optional.empty()

        adoptionRequestService.delete(1, 1)
            .test()
            .verifyErrorMatches {
                it is EntityNotFoundException && it.message == "Adoption request not found"
            }

        verify(exactly = 0) { adoptionRequestRepository.delete(any()) }
    }

    @Test
    fun `should throw InvalidAdoptionRequestStatusException if status is not PENDING`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customerId = 1,
            animalId = 1,
            status = AdoptionStatus.APPROVED
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns Optional.of(adoptionRequest)

        adoptionRequestService.delete(1, 1)
            .test()
            .verifyErrorMatches {
                it is InvalidAdoptionRequestStatusException && it.message == "Cannot delete adoption request with status: APPROVED"
            }

        verify(exactly = 0) { adoptionRequestRepository.delete(any()) }
    }
}
