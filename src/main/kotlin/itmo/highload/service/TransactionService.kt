@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import itmo.highload.model.Balance
import itmo.highload.model.User
import itmo.highload.repository.TransactionRepository
import itmo.highload.service.mapper.TransactionMapper
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService
) {

    private fun saveTransaction(donationDto: TransactionDto, customer: User, isDonation: Boolean): TransactionResponse {
        val balance: Balance = balanceService.getById(donationDto.purposeId)
        val transactionEntity = TransactionMapper.toEntity(donationDto, customer, balance, isDonation)
        val savedTransaction = transactionRepository.save(transactionEntity)
        return TransactionMapper.toResponse(savedTransaction)
    }

    fun saveDonation(donationDto: TransactionDto, customer: User): TransactionResponse {
        return saveTransaction(donationDto, customer, isDonation = true);
    }

    fun saveExpense(expenseDto: TransactionDto, customer: User): TransactionResponse {
        return saveTransaction(expenseDto, customer, isDonation = false);
    }
}
