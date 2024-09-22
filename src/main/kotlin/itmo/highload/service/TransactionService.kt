package itmo.highload.service

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.User
import itmo.highload.repository.BalanceRepository
import itmo.highload.repository.TransactionRepository
import itmo.highload.service.mapper.TransactionMapper
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceRepository: BalanceRepository,
    private val balanceService: BalanceService
) {

    private fun mapPageToResponse(page: Page<Transaction>): Page<TransactionResponse> {
        return page.map { transaction -> TransactionMapper.toResponse(transaction) }
    }

    fun getAll(isDonation: Boolean, pageable: Pageable): Page<TransactionResponse> {
        val page = transactionRepository.findByIsDonation(isDonation, pageable)
        return mapPageToResponse(page)
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, pageable: Pageable): Page<TransactionResponse> {
        val page = transactionRepository.findByIsDonationAndUserId(isDonation, userId, pageable)
        return mapPageToResponse(page)
    }

    fun getAllByPurpose(isDonation: Boolean, purposeId: Int, pageable: Pageable): Page<TransactionResponse> {
        val page = transactionRepository.findByIsDonationAndBalanceId(isDonation, purposeId, pageable)
        return mapPageToResponse(page)
    }

    @Transactional
    fun addTransaction(donationDto: TransactionDto, customer: User, isDonation: Boolean): TransactionResponse {
        val balance: Optional<Balance> = balanceRepository.findById(donationDto.purposeId)

        if (balance.isEmpty) {
            throw EntityNotFoundException("Failed to find Balance with id = ${donationDto.purposeId}")
        }

        val transactionEntity = TransactionMapper.toEntity(donationDto, customer, balance.get(), isDonation)
        val savedTransaction = transactionRepository.save(transactionEntity)

        balanceService.changeMoneyAmount(donationDto.purposeId, isDonation, donationDto.moneyAmount)

        return TransactionMapper.toResponse(savedTransaction)
    }
}
