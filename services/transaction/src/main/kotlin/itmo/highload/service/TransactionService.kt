package itmo.highload.service

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository, private val balanceService: BalanceService
) {

    fun getExpenses(purposeId: Int?): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(false, purposeId).flatMap { transaction ->
                    balanceService.getById(transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        } else {
            transactionRepository.findByIsDonation(false).flatMap { transaction ->
                    balanceService.getById(transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        }
    }

    fun getDonations(purposeId: Int?): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(true, purposeId).flatMap { transaction ->
                    balanceService.getById(transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        } else {
            transactionRepository.findByIsDonation(true).flatMap { transaction ->
                    balanceService.getById(transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        }
    }

    fun getAllByUser(isDonation: Boolean, userId: Int): Flux<TransactionResponse> =
        transactionRepository.findByIsDonationAndUserId(isDonation, userId).flatMap { transaction ->
                balanceService.getById(transaction.balanceId)
                    .map { balance -> TransactionMapper.toResponse(transaction, balance) }
            }

    fun addTransaction(donationDto: TransactionDto, managerId: Int, isDonation: Boolean): Mono<TransactionResponse> {
        return balanceService.getById(donationDto.purposeId!!).flatMap { balance ->
                val transactionEntity = TransactionMapper.toEntity(donationDto, managerId, balance, isDonation)
                transactionRepository.save(transactionEntity).flatMap { savedTransaction ->
                        balanceService.changeMoneyAmount(donationDto.purposeId!!, isDonation, donationDto.moneyAmount!!)
                            .thenReturn(savedTransaction)
                    }
            }.flatMap { transaction ->
                balanceService.getById(transaction.balanceId)
                    .map { balance -> TransactionMapper.toResponse(transaction, balance) }
            }
    }
}
