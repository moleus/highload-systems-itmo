package itmo.highload.domain.interactor

import itmo.highload.domain.BalanceRepository
import itmo.highload.domain.entity.BalanceEntity
import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.NegativeBalanceException

import itmo.highload.domain.mapper.BalanceMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

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

    fun changeMoneyAmount(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<BalanceEntity> {
        return balanceRepository.findById(id).flatMap { balance ->
            val updatedMoneyAmount = if (isDonation) {
                balance.moneyAmount + moneyAmount
            } else {
                balance.moneyAmount - moneyAmount
            }

            if (updatedMoneyAmount < 0) {
                Mono.error(NegativeBalanceException("Insufficient funds to complete the transaction"))
            } else {
                val updatedBalance = balance.copy(moneyAmount = updatedMoneyAmount)
                balanceRepository.save(updatedBalance)
                    .map { savedBalance -> BalanceMapper.toEntity(savedBalance) }
            }
        }
    }

    fun checkAndAdjustBalance(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Boolean> {
        return balanceRepository.findById(id).flatMap { balance ->
            val updatedMoneyAmount = if (isDonation) {
                balance.moneyAmount + moneyAmount
            } else {
                balance.moneyAmount - moneyAmount
            }

            if (updatedMoneyAmount < 0) {
                // Недостаточно средств, возвращаем false
                Mono.just(false)
            } else {
                // Обновляем баланс и сохраняем, возвращаем true
                val updatedBalance = balance.copy(moneyAmount = updatedMoneyAmount)
                balanceRepository.save(updatedBalance).thenReturn(true)
            }
        }.defaultIfEmpty(false) // Если баланс не найден, возвращаем false
    }

}
