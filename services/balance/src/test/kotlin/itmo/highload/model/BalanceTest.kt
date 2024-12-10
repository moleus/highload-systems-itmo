package itmo.highload.model

import itmo.highload.domain.entity.BalanceEntity
import itmo.highload.domain.mapper.BalanceMapper
import itmo.highload.infrastructure.postgres.model.Balance
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BalanceTest {

    @Test
    fun `should create Balance with valid data`() {
        val balance = Balance(purpose = "Медицина", moneyAmount = 500)

        assertNotNull(balance)
        assertEquals("Медицина", balance.purpose)
        assertEquals(500, balance.moneyAmount)
    }


    @Test
    fun `should map to BalanceResponse`() {
        val balance = BalanceEntity(purpose = "Медицина", moneyAmount = 500)
        val balanceResponse = BalanceMapper.toBalanceResponse(balance)

        assertEquals("Медицина", balanceResponse.purpose.name)
        assertEquals(500, balanceResponse.moneyAmount)
    }
}
