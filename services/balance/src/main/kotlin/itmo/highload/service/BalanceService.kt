package itmo.highload.service

import itmo.highload.exceptions.EntityAlreadyExistsException
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.model.Balance
import itmo.highload.model.BalanceMapper
import itmo.highload.repository.BalanceRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getBalanceById(id: Int): Mono<Balance> {
        return balanceRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Failed to find Balance with id = $id")))
    }

    fun getAll(): Flux<Balance> {
        return balanceRepository.findAll()
    }

    fun getAllPurposes(): Flux<Balance> {
        return balanceRepository.findAll()
    }

    fun addPurpose(name: String): Mono<Balance> {
        return balanceRepository.findByPurpose(name)
            .flatMap<Balance> { Mono.error(EntityAlreadyExistsException("Purpose with name '$name' already exists")) }
            .switchIfEmpty(Mono.defer { balanceRepository.save(BalanceMapper.toEntity(name)) })
    }

    fun changeMoneyAmount(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<Balance> {
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
