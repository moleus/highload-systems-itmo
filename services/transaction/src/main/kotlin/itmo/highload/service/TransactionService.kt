package itmo.highload.service

import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.kafka.TransactionProducer
import itmo.highload.model.TransactionMapper
import itmo.highload.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val balanceService: BalanceService,
//    private val transactionProducer: TransactionProducer
) {
//    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    fun getExpenses(purposeId: Int?, token: String): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(false, purposeId).flatMap { transaction ->
                    balanceService.getBalanceById(token, transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        } else {
            transactionRepository.findByIsDonation(false).flatMap { transaction ->
                    balanceService.getBalanceById(token, transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        }
    }

    fun getDonations(purposeId: Int?, token: String): Flux<TransactionResponse> {
        return if (purposeId != null) {
            transactionRepository.findByIsDonationAndBalanceId(true, purposeId).flatMap { transaction ->
                    balanceService.getBalanceById(token, transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        } else {
            transactionRepository.findByIsDonation(true).flatMap { transaction ->
                    balanceService.getBalanceById(token, transaction.balanceId)
                        .map { balance -> TransactionMapper.toResponse(transaction, balance) }
                }
        }
    }

    fun getAllByUser(isDonation: Boolean, userId: Int, token: String): Flux<TransactionResponse> =
        transactionRepository.findByIsDonationAndUserId(isDonation, userId).flatMap { transaction ->
                balanceService.getBalanceById(token, transaction.balanceId)
                    .map { balance -> TransactionMapper.toResponse(transaction, balance) }
            }

//    fun addTransaction(donationDto: TransactionDto, managerId: Int, isDonation: Boolean): Mono<TransactionResponse> {
//        return balanceService.getById(donationDto.purposeId!!).flatMap { balance ->
//                val transactionEntity = TransactionMapper.toEntity(donationDto, managerId, balance, isDonation)
//                transactionRepository.save(transactionEntity).flatMap { savedTransaction ->
//                        balanceService.changeMoneyAmount(donationDto.purposeId!!, isDonation, donationDto.moneyAmount!!)
//                            .thenReturn(savedTransaction)
//                    }
//            }.flatMap { transaction ->
//                balanceService.getById(transaction.balanceId)
//                    .map { balance ->
//                        val transactionResponse = TransactionMapper.toResponse(transaction, balance)
//
//                        if (isDonation) {
//                            val message = TransactionMapper.toResponse(transaction, balance)
//                            Mono.fromCallable { transactionProducer.sendMessageToNewDonationTopic(message) }
//                                .subscribeOn(Schedulers.boundedElastic())
//                                .onErrorContinue { error, _ ->
//                                    logger.error("Failed to send donation message to Kafka: ${error.message}")
//                                }
//                                .subscribe()
//                        }
//                        transactionResponse }
//            }
//    }
}

