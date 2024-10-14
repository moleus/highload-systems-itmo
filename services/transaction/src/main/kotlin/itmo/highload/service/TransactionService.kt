package itmo.highload.service

import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService
) {

    fun getExpenses(purposeId: Int?, pageable: Pageable): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(false, purposeId, pageable)
                .map { TransactionMapper.toResponse(it) }
        } else {
            transactionRepository.findByIsDonation(false, pageable)
                .map { TransactionMapper.toResponse(it) }
        }
    }

    fun getDonations(purposeId: Int?, pageable: Pageable): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(true, purposeId, pageable)
                .map { TransactionMapper.toResponse(it) }
        } else {
            transactionRepository.findByIsDonation(true, pageable)
                .map { TransactionMapper.toResponse(it) }
        }
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, pageable: Pageable): Flux<TransactionResponse> {
        return transactionRepository.findByIsDonationAndUserId(isDonation, userId, pageable)
            .map { TransactionMapper.toResponse(it) }
    }

    fun addTransaction(donationDto: TransactionDto, managerId: Int, isDonation: Boolean): Mono<TransactionResponse> {
        return balanceService.getById(donationDto.purposeId!!)
            .flatMap { balance ->
                val transactionEntity = TransactionMapper.toEntity(donationDto, managerId, balance, isDonation)
                transactionRepository.save(transactionEntity)
                    .flatMap { savedTransaction ->
                        balanceService.changeMoneyAmount(donationDto.purposeId!!, isDonation, donationDto.moneyAmount!!)
                            .thenReturn(savedTransaction)
                    }
            }
            .map { TransactionMapper.toResponse(it) }
    }
}
