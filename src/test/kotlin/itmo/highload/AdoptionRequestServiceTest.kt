package itmo.highload

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.Animal
import itmo.highload.model.Customer
import itmo.highload.model.Ownership
import itmo.highload.model.User
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.model.enum.Role
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.repository.OwnershipRepository
import itmo.highload.service.AdoptionRequestService
import itmo.highload.service.AnimalService
import itmo.highload.service.exception.EntityAlreadyExistsException
import itmo.highload.service.exception.InvalidAdoptionRequestStatusException
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class AdoptionRequestServiceTest {

    private val adoptionRequestRepository: AdoptionRequestRepository = mockk()
    private val customerRepository: CustomerRepository = mockk()
    private val animalService: AnimalService = mockk()
    private val ownershipRepository: OwnershipRepository = mockk()
    private val adoptionRequestService = AdoptionRequestService(
        adoptionRequestRepository, customerRepository, animalService, ownershipRepository
    )

    private val customer = Customer(
        id = 1,
        phone = "71234567890",
        gender = Gender.MALE,
        address = "123 Main St, City, Country"
    )

    private val animal = Animal(
        id = 1,
        name = "Buddy",
        typeOfAnimal = "Dog",
        gender = Gender.MALE,
        isCastrated = true,
        healthStatus = HealthStatus.HEALTHY
    )

    private val manager = User(
        id = 1,
        login = "manager",
        password = "123",
        role = Role.ADOPTION_MANAGER,
        creationDate = LocalDate.now()
    )

    @Test
    fun `should save adoption request if no existing request found`() {

        val adoptionRequest = AdoptionRequest(
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns null
        every { customerRepository.findById(1) } returns Optional.of(customer)
        every { animalService.getById(1) } returns animal
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest

        val result = adoptionRequestService.save(1, 1)

        assertEquals(adoptionRequest, result)
        verify { adoptionRequestRepository.save(any()) }
    }

    @Test
    fun `should throw EntityAlreadyExistsException if adoption request already exists`() {

        val existingRequest = AdoptionRequest(
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns existingRequest

        val exception = assertThrows<EntityAlreadyExistsException> {
            adoptionRequestService.save(1, 1)
        }

        assertEquals("An adoption request already exists for customer ID: 1 and animal ID: 1", exception.message)
    }

    @Test
    fun `should throw EntityNotFoundException if customer not found`() {
        val customerId = 1
        val animalId = 1

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(customerId, animalId) } returns null
        every { customerRepository.findById(customerId) } returns Optional.empty()

        val exception = assertThrows<EntityNotFoundException> {
            adoptionRequestService.save(customerId, animalId)
        }

        assertEquals("Customer not found", exception.message)
    }

    @Test
    fun `should update adoption request status to DENIED without creating ownership`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.PENDING
        )
        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.DENIED)

        every { adoptionRequestRepository.findById(1) } returns Optional.of(adoptionRequest)
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest

        val result = adoptionRequestService.update(manager, requestDto)

        assertEquals(AdoptionStatus.DENIED, result.status)
        assertEquals(manager, result.manager)
        verify(exactly = 0) { ownershipRepository.save(any()) }
    }

    @Test
    fun `should create ownership when adoption request is approved`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.PENDING
        )
        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.APPROVED)

        every { adoptionRequestRepository.findById(1) } returns Optional.of(adoptionRequest)
        every { adoptionRequestRepository.save(any()) } returns adoptionRequest
        every { ownershipRepository.save(any()) } returns Ownership(adoptionRequest.customer, adoptionRequest.animal)

        val result = adoptionRequestService.update(manager, requestDto)

        assertEquals(AdoptionStatus.APPROVED, result.status)
        verify { ownershipRepository.save(any()) }
        verify { adoptionRequestRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when adoption request is not found`() {

        val requestDto = UpdateAdoptionRequestStatusDto(id = 1, status = AdoptionStatus.APPROVED)

        every { adoptionRequestRepository.findById(1) } returns Optional.empty()

        val exception = assertThrows<EntityNotFoundException> {
            adoptionRequestService.update(manager, requestDto)
        }

        assertEquals("Adoption request not found", exception.message)
        verify(exactly = 0) { ownershipRepository.save(any()) }
        verify(exactly = 0) { adoptionRequestRepository.save(any()) }
    }

    @Test
    fun `should delete adoption request if status is PENDING`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.PENDING
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns adoptionRequest
        every { adoptionRequestRepository.delete(adoptionRequest) } returns Unit

        adoptionRequestService.delete(1, 1)

        verify { adoptionRequestRepository.delete(adoptionRequest) }
    }

    @Test
    fun `should throw EntityNotFoundException if adoption request is not found`() {
        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns null

        val exception = assertThrows<EntityNotFoundException> {
            adoptionRequestService.delete(1, 1)
        }

        assertEquals("Adoption request not found", exception.message)
        verify(exactly = 0) { adoptionRequestRepository.delete(any()) }
    }

    @Test
    fun `should throw InvalidAdoptionRequestStatusException if status is not PENDING`() {

        val adoptionRequest = AdoptionRequest(
            id = 1,
            dateTime = LocalDateTime.now(),
            customer = customer,
            animal = animal,
            status = AdoptionStatus.APPROVED
        )

        every { adoptionRequestRepository.findByCustomerIdAndAnimalId(1, 1) } returns adoptionRequest

        val exception = assertThrows<InvalidAdoptionRequestStatusException> {
            adoptionRequestService.delete(1, 1)
        }

        assertEquals("Cannot delete adoption request with status: APPROVED", exception.message)
        verify(exactly = 0) { adoptionRequestRepository.delete(any()) }
    }
}
