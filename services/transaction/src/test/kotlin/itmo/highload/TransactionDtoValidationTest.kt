package itmo.highload

import itmo.highload.api.dto.TransactionDto
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.ConstraintViolation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionDtoValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should fail validation when purposeId is null`() {
        val transactionDto = TransactionDto(
            purposeId = null,
            moneyAmount = 100
        )

        val violations: Set<ConstraintViolation<TransactionDto>> = validator.validate(transactionDto)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("не должно равняться null", violation.message)
        assertEquals("purposeId", violation.propertyPath.toString())
    }

    @Test
    fun `should fail validation when moneyAmount is null`() {
        val transactionDto = TransactionDto(
            purposeId = 1,
            moneyAmount = null
        )

        val violations: Set<ConstraintViolation<TransactionDto>> = validator.validate(transactionDto)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("не должно равняться null", violation.message)
        assertEquals("moneyAmount", violation.propertyPath.toString())
    }

    @Test
    fun `should fail validation when moneyAmount is less than 1`() {
        val transactionDto = TransactionDto(
            purposeId = 1,
            moneyAmount = 0
        )

        val violations: Set<ConstraintViolation<TransactionDto>> = validator.validate(transactionDto)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("должно быть не меньше 1", violation.message)
        assertEquals("moneyAmount", violation.propertyPath.toString())
    }

    @Test
    fun `should pass validation when all fields are valid`() {
        val transactionDto = TransactionDto(
            purposeId = 1,
            moneyAmount = 100
        )

        val violations: Set<ConstraintViolation<TransactionDto>> = validator.validate(transactionDto)

        assertEquals(0, violations.size)
    }
}
