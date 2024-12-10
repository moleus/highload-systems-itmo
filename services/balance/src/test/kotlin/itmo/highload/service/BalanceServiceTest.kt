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

    private val balanceMap: IMap<Int, BalanceEntity> = mockk()

    init {
        every { hazelcastInstance.getMap<Int, BalanceEntity>("balance") } returns balanceMap
    }

    @Test
    fun `should throw EntityNotFoundException when balance by id is not found`() {
        every { balanceRepository.findById(1) } returns Mono.empty()
        every { balanceMap[1] } returns null

        val result = balanceService.getBalanceById(1)

        StepVerifier.create(result)
            .expectErrorMatches { it is EntityNotFoundException && it.message == "Balance with ID 1 not found" }
            .verify()

        verify { balanceRepository.findById(1) }
    }


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

    @Test
    fun `should retrieve all balances and populate cache`() {
        every { balanceRepository.findAll() } returns Flux.just(testBalance)
        every { balanceMap[1] } returns null
        every { balanceMap[1] = testBalanceEntity } returns Unit

        val result = balanceService.getAll()

        StepVerifier.create(result)
            .expectNextMatches { it.id == testBalance.id && it.purpose == testBalance.purpose }
            .verifyComplete()

        verify { balanceRepository.findAll() }
        verify { balanceMap[1] = testBalanceEntity }
    }

    @Test
    fun `should retrieve all purposes`() {
        every { balanceRepository.findAll() } returns Flux.just(testBalance)
        every { balanceMap[1] } returns null
        every { balanceMap[1] = testBalanceEntity } returns Unit

        val result = balanceService.getAllPurposes()

        StepVerifier.create(result)
            .expectNextMatches { it.id == testBalance.id && it.purpose == testBalance.purpose }
            .verifyComplete()

        verify { balanceRepository.findAll() }
        verify { balanceMap[1] = testBalanceEntity }
    }

    @Test
    fun `should successfully adjust balance for donation`() {
        val adjustedBalance = testBalance.copy(moneyAmount = testBalance.moneyAmount + 50)
        val adjustedEntity = BalanceMapper.toEntity(adjustedBalance)

        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
        every { balanceMap[1] } returns testBalanceEntity
        every { balanceRepository.save(any<Balance>()) } returns Mono.just(adjustedBalance)
        every { balanceMap.computeIfPresent(1, any()) } answers {
            adjustedEntity
        }

        val result = balanceService.checkAndAdjustBalance(1, isDonation = true, moneyAmount = 50)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
        verify { balanceRepository.save(any<Balance>()) }
        verify { balanceMap.computeIfPresent(1, any()) }
    }

    @Test
    fun `should successfully rollback balance for non-donation`() {
        val adjustedBalance = testBalance.copy(moneyAmount = testBalance.moneyAmount + 50)
        val adjustedEntity = BalanceMapper.toEntity(adjustedBalance)

        every { balanceRepository.findById(1) } returns Mono.just(testBalance)
        every { balanceMap[1] } returns testBalanceEntity
        every { balanceRepository.save(any<Balance>()) } returns Mono.just(adjustedBalance)
        every { balanceMap.computeIfPresent(1, any()) } answers {
            adjustedEntity
        }

        val result = balanceService.rollbackBalance(1, isDonation = false, moneyAmount = 50)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { balanceRepository.findById(1) }
        verify { balanceRepository.save(any<Balance>()) }
        verify { balanceMap.computeIfPresent(1, any()) }
    }


}
