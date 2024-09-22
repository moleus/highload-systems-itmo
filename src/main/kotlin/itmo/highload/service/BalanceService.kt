package itmo.highload.service

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.exception.NegativeBalanceException
import itmo.highload.service.mapper.BalanceMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getById(id: Int): BalanceResponse {
        val balance = balanceRepository.findById(id)
        if (balance.isPresent) {
            return BalanceMapper.toBalanceResponse(balance.get())
        }
        throw EntityNotFoundException("Failed to find Balance with id = $id")
    }

    fun getAll(pageable: Pageable): Page<BalanceResponse> {
        val page: Page<Balance> = balanceRepository.findAll(pageable)
        return page.map { balance -> BalanceMapper.toBalanceResponse(balance) }
    }

    fun getAllPurposes(pageable: Pageable): Page<PurposeResponse> {
        val page: Page<Balance> = balanceRepository.findAll(pageable)
        return page.map { balance -> BalanceMapper.toPurposeResponse(balance) }
    }

    fun addPurpose(name: String): PurposeResponse {
        val balance = balanceRepository.save(BalanceMapper.toEntity(name))
        return BalanceMapper.toPurposeResponse(balance)
    }

    fun changeMoneyAmount(id: Int, isDonation: Boolean, moneyAmount: Int) {
        val balanceOptional = balanceRepository.findById(id)

        if (balanceOptional.isEmpty) {
            throw EntityNotFoundException("Failed to find Balance with id = $id")
        }

        val balance = balanceOptional.get()

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
