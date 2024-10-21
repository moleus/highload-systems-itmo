package itmo.highload

import itmo.highload.api.dto.TransactionDto
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.ConstraintViolation
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        assertTrue(violation.constraintDescriptor.annotation is NotNull)
        assertEquals("purposeId", violation.propertyPath.toString())
        assertEquals("must not be null", violation.message)
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

        assertTrue(violation.constraintDescriptor.annotation is NotNull)
        assertEquals("moneyAmount", violation.propertyPath.toString())
        assertEquals("must not be null", violation.message)
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

        assertTrue(violation.constraintDescriptor.annotation is Min)
        assertEquals("moneyAmount", violation.propertyPath.toString())
        assertEquals("must be greater than or equal to 1", violation.message)
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
