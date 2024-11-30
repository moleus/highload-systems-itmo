package itmo.highload.service

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.BalanceRepository
import itmo.highload.domain.entity.BalanceEntity
import itmo.highload.domain.interactor.BalanceService
import itmo.highload.domain.mapper.BalanceMapper
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.infrastructure.postgres.model.Balance
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class BalanceServiceTest {

    private val balanceRepository: BalanceRepository = mockk()
    private val delay: Long = 1
    private val hazelcastInstance: HazelcastInstance = mockk()
    private val balanceService = BalanceService(balanceRepository, hazelcastInstance, delay)

    private val testBalance = Balance(id = 1, purpose = "test", moneyAmount = 100)
    private val testBalanceEntity = BalanceMapper.toEntity(testBalance)

    // Mocking the Hazelcast IMap
    private val balanceMap: IMap<Int, BalanceEntity> = mockk()

    init {
        // When getMap is called, return the mocked map
        every { hazelcastInstance.getMap<Int, BalanceEntity>("balance") } returns balanceMap
    }
//
//    @Test
//    fun `should return balance by id`() {
//        // Mock repository to return the balance
//        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
//
//        // Simulate cache miss
//        every { balanceMap[1] } returns null
//
//        // Mock the `put` method to do nothing or return a value
//        every { balanceMap.put(1, any()) } returns null
//
//        // Call the service method
//        val result = balanceService.getBalanceById(1)
//
//        // Verify the result
//        StepVerifier.create(result)
//            .assertNext { assertEquals(testBalanceEntity, it) }
//            .verifyComplete()
//
//        // Verify interactions
//        verify { balanceRepository.findById(1) }
//        verify { balanceMap.put(1, testBalanceEntity) }
//    }


    @Test
    fun `should throw EntityNotFoundException when balance by id is not found`() {
        every { balanceRepository.findById(1) } returns Mono.empty()
        every { balanceMap[1] } returns null // Simulating cache miss

        val result = balanceService.getBalanceById(1)

        StepVerifier.create(result)
            .expectErrorMatches { it is EntityNotFoundException && it.message == "Balance with ID 1 not found" }
            .verify()

        verify { balanceRepository.findById(1) }
    }

//    @Test
//    fun `should return all balances`() {
//        every { balanceRepository.findAll() } returns Flux.just(testBalance)
//        every { balanceMap[1] } returns null // Simulating cache miss
//
//        val result = balanceService.getAll()
//
//        StepVerifier.create(result)
//            .assertNext { assertEquals(testBalanceEntity, it) }
//            .verifyComplete()
//
//        verify { balanceRepository.findAll() }
//        verify { balanceMap.put(1, testBalanceEntity) }
//    }
//
//    @Test
//    fun `should add purpose successfully`() {
//        every { balanceRepository.findByPurpose("test") } returns Mono.empty()
//        every { balanceRepository.save(any()) } returns Mono.just(testBalance)
//        every { balanceMap[1] } returns null // Simulating cache miss
//
//        val result = balanceService.addPurpose("test")
//
//        StepVerifier.create(result)
//            .assertNext { assertEquals(testBalanceEntity, it) }
//            .verifyComplete()
//
//        verify { balanceRepository.findByPurpose("test") }
//        verify { balanceRepository.save(any()) }
//        verify { balanceMap.put(1, testBalanceEntity) }
//    }

    @Test
    fun `should throw EntityAlreadyExistsException when purpose already exists`() {
        every { balanceRepository.findByPurpose("test") } returns Mono.just(testBalance)
        every { balanceMap[1] } returns null // Simulating cache miss

        val result = balanceService.addPurpose("test")

        StepVerifier.create(result)
            .expectErrorMatches { it is EntityAlreadyExistsException && it.message ==
                    "Purpose with name 'test' already exists" }
            .verify()

        verify { balanceRepository.findByPurpose("test") }
    }

//    @Test
//    fun `should adjust balance correctly for donation`() {
//        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
//        every { balanceRepository.save(any()) } returns Mono.just(testBalance.copy(moneyAmount = 150))
//        every { balanceMap[1] } returns testBalanceEntity // Simulating cache hit
//
//        val result = balanceService.checkAndAdjustBalance(1, isDonation = true, moneyAmount = 50)
//
//        StepVerifier.create(result)
//            .expectNext(true)
//            .verifyComplete()
//
//        verify { balanceRepository.findById(1) }
//        verify { balanceRepository.save(testBalance.copy(moneyAmount = 150)) }
//        verify { balanceMap.put(1, testBalanceEntity.copy(moneyAmount = 150)) }
//    }

    @Test
    fun `should fail to adjust balance for negative amount`() {
        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
        every { balanceMap[1] } returns testBalanceEntity // Simulating cache hit

        val result = balanceService.checkAndAdjustBalance(1, isDonation = false, moneyAmount = 200)

        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
    }

//    @Test
//    fun `should rollback balance correctly`() {
//        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
//        every { balanceRepository.save(any()) } returns Mono.just(testBalance.copy(moneyAmount = 50))
//        every { balanceMap[1] } returns testBalanceEntity // Simulating cache hit
//
//        val result = balanceService.rollbackBalance(1, isDonation = true, moneyAmount = 50)
//
//        StepVerifier.create(result)
//            .expectNext(true)
//            .verifyComplete()
//
//        verify { balanceRepository.findById(1) }
//        verify { balanceRepository.save(testBalance.copy(moneyAmount = 50)) }
//        verify { balanceMap.put(1, testBalanceEntity.copy(moneyAmount = 50)) }
//    }

}
