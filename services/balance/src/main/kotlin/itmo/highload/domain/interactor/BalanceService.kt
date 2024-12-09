package itmo.highload.domain.interactor

import com.hazelcast.core.HazelcastInstance
import itmo.highload.domain.BalanceRepository
import itmo.highload.domain.entity.BalanceEntity
import itmo.highload.domain.mapper.BalanceMapper
import itmo.highload.exceptions.EntityAlreadyExistsException
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class BalanceService(private val balanceRepository: BalanceRepository,
                     private val hazelcastInstance: HazelcastInstance,
                     @Value("\${balance.delay}")
                     val delay: Long
) {
    private val balanceCacheName = "balance"

    fun getBalanceById(id: Int): Mono<BalanceEntity> {
        val cache = hazelcastInstance.getMap<Int, BalanceEntity>(balanceCacheName)
        val cachedBalance = cache[id]
        return if (cachedBalance != null) {
            Mono.just(cachedBalance)
        } else {
            balanceRepository.findById(id)
                .switchIfEmpty(Mono.error(EntityNotFoundException("Balance with ID $id not found")))
                .map { balance ->
                    val entity = BalanceMapper.toEntity(balance)
                    cache[id] = entity
                    entity
                }
        }
    }

    fun getAll(): Flux<BalanceEntity> {
        val cache = hazelcastInstance.getMap<Int, BalanceEntity>(balanceCacheName)
        return balanceRepository.findAll()
            .map { balance ->
                val entity = BalanceMapper.toEntity(balance)
                cache[balance.id] = entity
                entity
            }
    }

    fun getAllPurposes(): Flux<BalanceEntity> {
        return getAll()
    }

    fun addPurpose(name: String): Mono<BalanceEntity> {
        val cache = hazelcastInstance.getMap<Int, BalanceEntity>(balanceCacheName)
        return balanceRepository.findByPurpose(name)
            .flatMap<BalanceEntity> {
                Mono.error(EntityAlreadyExistsException("Purpose with name '$name' already exists"))
            }.switchIfEmpty(
                Mono.defer { balanceRepository.save(BalanceMapper.toJpaEntity(name)) }
                    .map { balance ->
                        val entity = BalanceMapper.toEntity(balance)
                        cache[balance.id] = entity
                        entity
                    }
            )
    }


    fun checkAndAdjustBalance(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Boolean> {
        val cache = hazelcastInstance.getMap<Int, BalanceEntity>(balanceCacheName)
        return balanceRepository.findById(id).flatMap { balance ->
            val updatedMoneyAmount = if (isDonation) {
                balance.moneyAmount + moneyAmount
            } else {
                balance.moneyAmount - moneyAmount
            }

            Mono.delay(Duration.ofSeconds(delay))
                .then(
                    if (updatedMoneyAmount < 0) {
                        Mono.just(false)
                    } else {
                        val updatedBalance = balance.copy(moneyAmount = updatedMoneyAmount)
                        balanceRepository.save(updatedBalance).thenReturn(true)
                            .doOnSuccess {
                                cache.computeIfPresent(id) { _, cachedBalance ->
                                    cachedBalance.copy(moneyAmount = updatedMoneyAmount)
                                }
                            }
                            .thenReturn(true)
                    }
                )
        }.defaultIfEmpty(false)
    }

    fun rollbackBalance(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Boolean> {
        val cache = hazelcastInstance.getMap<Int, BalanceEntity>(balanceCacheName)
        return Mono.delay(Duration.ofSeconds(delay))
            .then(
                balanceRepository.findById(id).flatMap { balance ->
                    val updatedMoneyAmount = if (isDonation) {
                        balance.moneyAmount - moneyAmount
                    } else {
                        balance.moneyAmount + moneyAmount
                    }
                    if (updatedMoneyAmount < 0) {
                        Mono.just(false)
                    } else {
                        val updatedBalance = balance.copy(moneyAmount = updatedMoneyAmount)
                        balanceRepository.save(updatedBalance).thenReturn(true)
                            .doOnSuccess {
                                cache.computeIfPresent(id) { _, cachedBalance ->
                                    cachedBalance.copy(moneyAmount = updatedMoneyAmount)
                                }
                            }
                            .thenReturn(true)
                    }
                }.defaultIfEmpty(false)
            )
    }

}
