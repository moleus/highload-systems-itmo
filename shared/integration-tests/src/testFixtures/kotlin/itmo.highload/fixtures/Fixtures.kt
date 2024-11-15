@file:Suppress("LongParameterList")

package itmo.highload.fixtures

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.api.dto.response.*
import java.time.LocalDateTime

object BalanceResponseFixture {
    fun of(
        purpose: PurposeResponse = PurposeResponse(-1, "Medicine"), moneyAmount: Int = 1000
    ) = BalanceResponse(-1, purpose, moneyAmount)
}

object PurposeResponseFixture {
    fun of(
        id: Int = -1, name: String = "Medicine"
    ) = PurposeResponse(id, name)
}

object AnimalResponseFixture {
    fun of(
        id: Int = -1,
        name: String = "Buddy",
        type: String = "Dog",
        gender: Gender = Gender.MALE,
        isCastrated: Boolean = true,
        healthStatus: HealthStatus = HealthStatus.HEALTHY
    ) = AnimalResponse(id, name, type, gender, isCastrated, healthStatus)
}

object CustomerResponseFixture {
    fun of(
        id: Int = -2, phone: String = "+79444333141", gender: Gender = Gender.MALE, address: String = "Moscow"
    ) = CustomerResponse(id, phone, gender, address)
}

object TransactionResponseFixture {
    fun of(
        dateTime: LocalDateTime = LocalDateTime.parse("2023-01-01T00:00:00"),
        purpose: PurposeResponse = PurposeResponse(-1, "Medicine"),
        userId: Int = -2,
        moneyAmount: Int = 100,
        isDonation: Boolean = true
    ) = TransactionResponse(dateTime, purpose, userId, moneyAmount, isDonation, "PENDING")
}

object AdoptionRequestResponseFixture {
    fun of(
        id: Int = -1,
        dateTime: LocalDateTime = LocalDateTime.parse("2023-01-01T00:00:00"),
        status: AdoptionStatus = AdoptionStatus.PENDING,
        customerId: Int = -2,
        managerId: Int? = null,
        animalId: Int = -1
    ) = AdoptionRequestResponse(id, dateTime, status, customerId, managerId, animalId)
}
