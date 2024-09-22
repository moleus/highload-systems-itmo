package itmo.highload.service

import itmo.highload.mapper.BalanceMapper
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.exception.NegativeBalanceException
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getById(id: Int): Balance {
        val balance = balanceRepository.findById(id)

        if (balance.isPresent) {
            return balance.get()
        }

        throw EntityNotFoundException("Failed to find Balance with id = $id")
    }

    fun getAll(pageable: Pageable): Page<Balance> {
        return balanceRepository.findAll(pageable)
    }

    fun getAllPurposes(pageable: Pageable): Page<Balance> {
        return balanceRepository.findAll(pageable)
    }

    fun addPurpose(name: String): Balance {
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
