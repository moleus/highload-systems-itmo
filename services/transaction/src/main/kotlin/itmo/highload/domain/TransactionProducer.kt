package itmo.highload.domain

import itmo.highload.api.dto.response.TransactionResponse
import itmo.highload.kafka.TransactionBalanceMessage

interface TransactionProducer {
    fun sendMessageToNewDonationTopic(transaction: TransactionResponse)
    fun sendMessageToBalanceCheck(transaction: TransactionBalanceMessage)
    fun sendRollBackMessage(transaction: TransactionBalanceMessage)
}
