package itmo.highload.model


import itmo.highload.api.dto.TransactionDto
import itmo.highload.api.dto.response.BalanceResponse
import itmo.highload.api.dto.response.PurposeResponse
import itmo.highload.domain.mapper.TransactionMapper
import itmo.highload.infrastructure.postgres.model.Transaction
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionMapperTest {

    @Test
    fun `toEntity should map TransactionDto to Transaction entity`() {
        val dto = TransactionDto(
            moneyAmount = 100,
            purposeId = 1
        )
        val balanceResponse = BalanceResponse(id = 1, purpose = PurposeResponse(id = 1, name = "Test Purpose"),
            moneyAmount = 100)

        val transactionEntity = TransactionMapper.toEntity(dto, userId = 1, balance = balanceResponse,
            isDonation = true)

        assertNotNull(transactionEntity)
        assertEquals(transactionEntity.moneyAmount, 100)
        assertEquals(transactionEntity.balanceId, 1)
        assertEquals(transactionEntity.isDonation, true)
        assertEquals(transactionEntity.status, "PENDING")
    }

    @Test
    fun `toResponse should map Transaction entity to TransactionResponse`() {
        val entity = Transaction(
            id = 1,
            dateTime = LocalDateTime.now(),
            userId = 1,
            balanceId = 1,
            moneyAmount = 100,
            isDonation = true,
            status = "PENDING"
        )
        val balanceResponse = BalanceResponse(id = 1, purpose = PurposeResponse(id = 1, name = "Test Purpose"),
            moneyAmount = 100)

        val transactionResponse = TransactionMapper.toResponse(entity, balanceResponse)

        assertNotNull(transactionResponse)
        assertEquals(transactionResponse.moneyAmount, 100)
        assertEquals(transactionResponse.purpose.id, 1)
        assertEquals(transactionResponse.isDonation, true)
        assertEquals(transactionResponse.status, "PENDING")
    }

    @Test
    fun `toBalanceMessage should map Transaction entity to TransactionBalanceMessage`() {
        val entity = Transaction(
            id = 1,
            dateTime = LocalDateTime.now(),
            userId = 1,
            balanceId = 1,
            moneyAmount = 100,
            isDonation = true,
            status = "PENDING"
        )

        val balanceMessage = TransactionMapper.toBalanceMessage(entity)

        assertNotNull(balanceMessage)
        assertEquals(balanceMessage.transactionId, 1)
        assertEquals(balanceMessage.balanceId, 1)
        assertEquals(balanceMessage.moneyAmount, 100)
        assertEquals(balanceMessage.isDonation, true)
    }

    @Test
    fun `toResponseFromTransaction should map Transaction entity to TransactionResponse with null purpose name`() {
        val entity = Transaction(
            id = 1,
            dateTime = LocalDateTime.now(),
            userId = 1,
            balanceId = 1,
            moneyAmount = 100,
            isDonation = true,
            status = "PENDING"
        )

        val transactionResponse = TransactionMapper.toResponseFromTransaction(entity)

        assertNotNull(transactionResponse)
        assertEquals(transactionResponse.moneyAmount, 100)
        assertEquals(transactionResponse.purpose.id, 1)
        assertEquals(transactionResponse.purpose.name, null)
        assertEquals(transactionResponse.isDonation, true)
    }
}
