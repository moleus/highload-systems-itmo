package itmo.highload.domain.interactor

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
                     @Value("\${balance.delay}")
                     val delay: Long
) {

    fun getBalanceById(id: Int): Mono<BalanceEntity> {
        return balanceRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Failed to find Balance with id = $id")))
            .map { balance -> BalanceMapper.toEntity(balance) }
    }

    fun getAll(): Flux<BalanceEntity> {
        return balanceRepository.findAll()
            .map { balance -> BalanceMapper.toEntity(balance) }
    }

    fun getAllPurposes(): Flux<BalanceEntity> {
        return balanceRepository.findAll()
            .map { balance -> BalanceMapper.toEntity(balance) }
    }

    fun addPurpose(name: String): Mono<BalanceEntity> {
        return balanceRepository.findByPurpose(name)
            .flatMap<BalanceEntity> {
                Mono.error(EntityAlreadyExistsException("Purpose with name '$name' already exists"))
            }.switchIfEmpty(Mono.defer { balanceRepository.save(BalanceMapper.toJpaEntity(name)) }
                .map { balance -> BalanceMapper.toEntity(balance) })
    }

    fun checkAndAdjustBalance(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Boolean> {
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
                    }
                )
        }.defaultIfEmpty(false)
    }

    fun rollbackBalance(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Boolean> {
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
                    }
                }.defaultIfEmpty(false)
            )
    }

}
