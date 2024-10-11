package itmo.highload.service

import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.exceptions.NegativeBalanceException
import itmo.highload.model.Balance
import itmo.highload.model.BalanceMapper
import itmo.highload.repository.BalanceRepository
import itmo.highload.exceptions.EntityAlreadyExistsException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getById(id: Int): Mono<Balance> {
        return balanceRepository.findById(id)
    }

    fun getAll(): Flux<BalanceResponse> {
        return balanceRepository.findAll().map { BalanceMapper.toBalanceResponse(it) }
    }

    fun getAllPurposes(): Flux<PurposeResponse> {
        return balanceRepository.findAll().map { BalanceMapper.toPurposeResponse(it) }
    }

    fun addPurpose(name: String): Mono<Balance> {
        return balanceRepository.findByPurpose(name)
            .flatMap<Balance> { Mono.error(EntityAlreadyExistsException("Purpose with name '$name' already exists")) }
            .switchIfEmpty(balanceRepository.save(BalanceMapper.toEntity(name)))
    }

    fun changeMoneyAmount(id: Int, isDonation: Boolean, moneyAmount: Int): Mono<BalanceResponse> {
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
                balanceRepository.save(updatedBalance).map { BalanceMapper.toBalanceResponse(it) }
            }
        }
    }
}
