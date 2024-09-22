package itmo.highload.service

import itmo.highload.dto.TransactionDto
import itmo.highload.mapper.TransactionMapper
import itmo.highload.model.Balance
import itmo.highload.model.Transaction
import itmo.highload.model.User
import itmo.highload.repository.TransactionRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService
) {

    fun getAll(isDonation: Boolean, pageable: Pageable): Page<Transaction> {
        return transactionRepository.findByIsDonation(isDonation, pageable)
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, pageable: Pageable): Page<Transaction> {
        return transactionRepository.findByIsDonationAndUserId(isDonation, userId, pageable)
    }

    fun getAllByPurpose(isDonation: Boolean, purposeId: Int, pageable: Pageable): Page<Transaction> {
        return transactionRepository.findByIsDonationAndBalanceId(isDonation, purposeId, pageable)
    }

    @Transactional
    fun addTransaction(donationDto: TransactionDto, customer: User, isDonation: Boolean): Transaction {
        val balance: Balance = balanceService.getById(donationDto.purposeId)

        val transactionEntity = TransactionMapper.toEntity(donationDto, customer, balance, isDonation)
        val savedTransaction = transactionRepository.save(transactionEntity)

        balanceService.changeMoneyAmount(donationDto.purposeId, isDonation, donationDto.moneyAmount)

        return savedTransaction
    }
}
