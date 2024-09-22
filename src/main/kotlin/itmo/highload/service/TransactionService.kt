@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.TransactionDto
import itmo.highload.dto.response.TransactionResponse
import org.springframework.stereotype.Service

@Service
class TransactionService {
    fun saveDonation(donationDto: TransactionDto): TransactionResponse {
        return null!!
    }

    fun saveExpense(expenseDto: TransactionDto): TransactionResponse {
        return null!!
    }
}
