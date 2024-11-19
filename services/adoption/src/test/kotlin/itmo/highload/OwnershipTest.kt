package itmo.highload

import itmo.highload.model.Ownership
import itmo.highload.model.OwnershipId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OwnershipTest {

    @Test
    fun `should correctly initialize Ownership`() {
        val ownership = Ownership(customerId = 1, animalId = 101)

        assertEquals(1, ownership.customerId)
        assertEquals(101, ownership.animalId)
    }

    @Test
    fun `should correctly initialize OwnershipId`() {
        val ownershipId = OwnershipId(customerId = 1, animalId = 101)

        assertEquals(1, ownershipId.customerId)
        assertEquals(101, ownershipId.animalId)
    }
}