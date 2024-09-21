@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.DonationDto
import itmo.highload.dto.ExpenseDto
import itmo.highload.dto.response.TransactionResponse
import org.springframework.stereotype.Service

@Service
class TransactionService {
    fun saveDonation(donationDto: DonationDto): TransactionResponse {
        return null!!
    }

    fun saveExpense(expenseDto: ExpenseDto): TransactionResponse {
        return null!!
    }
}
