package itmo.highload.model

import itmo.highload.domain.entity.TransactionEntity
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionEntityTest {

    @Test
    fun `TransactionEntity should initialize correctly`() {
        val transaction = TransactionEntity(
            id = 1,
            dateTime = LocalDateTime.now(),
            userId = 1,
            balanceId = 1,
            moneyAmount = 100,
            isDonation = true,
            status = "PENDING"
        )

        assertNotNull(transaction)
        assertEquals(transaction.id, 1)
        assertEquals(transaction.userId, 1)
        assertEquals(transaction.balanceId, 1)
        assertEquals(transaction.moneyAmount, 100)
        assertEquals(transaction.isDonation, true)
        assertEquals(transaction.status, "PENDING")
    }
}
