package itmo.highload.service

import itmo.highload.mapper.BalanceMapper
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.exception.EntityAlreadyExistsException
import itmo.highload.service.exception.NegativeBalanceException
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getById(id: Int): Balance {
        return balanceRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Failed to find Balance with id = $id") }
    }

    fun getAll(pageable: Pageable): Page<Balance> {
        return balanceRepository.findAll(pageable)
    }

    fun addPurpose(name: String): Balance {
        if (balanceRepository.findByPurpose(name) != null) {
            throw EntityAlreadyExistsException("Purpose with name '$name' already exists")
        }
        return balanceRepository.save(BalanceMapper.toEntity(name))
    }

    fun changeMoneyAmount(id: Int, isDonation: Boolean, moneyAmount: Int) {
        val balance = getById(id)

        if (isDonation) {
            balance.moneyAmount += moneyAmount
        } else {
            balance.moneyAmount -= moneyAmount
        }

        if (balance.moneyAmount < 0) {
            throw NegativeBalanceException("Insufficient funds to complete the transaction")
        }
        balanceRepository.save(balance)
    }
}
