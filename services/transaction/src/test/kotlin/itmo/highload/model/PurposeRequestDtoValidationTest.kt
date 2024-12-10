package itmo.highload.model

import itmo.highload.api.dto.PurposeRequestDto
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PurposeRequestDtoValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should fail validation when purposeId is blank`() {
        val purposeRequestDto = PurposeRequestDto(
            name = ""
        )

        val violations: Set<ConstraintViolation<PurposeRequestDto>> = validator.validate(purposeRequestDto)

        assertEquals(2, violations.size)
        val notBlankViolation = violations.firstOrNull {
            it.constraintDescriptor.annotation.annotationClass.simpleName == "NotBlank"
        }
        assertTrue(notBlankViolation?.constraintDescriptor?.annotation is NotBlank)
        assertEquals("must not be empty", notBlankViolation?.message)
    }

    @Test
    fun `should fail validation when purposeId is too long`() {
        val purposeRequestDto = PurposeRequestDto(
            name = "a".repeat(51)
        )

        val violations: Set<ConstraintViolation<PurposeRequestDto>> = validator.validate(purposeRequestDto)

        assertEquals(1, violations.size)
        val notBlankViolation = violations.first()
        assertTrue(notBlankViolation.constraintDescriptor.annotation is Size)
        assertEquals("size must be between 1 and 50", notBlankViolation.message)
    }
}
