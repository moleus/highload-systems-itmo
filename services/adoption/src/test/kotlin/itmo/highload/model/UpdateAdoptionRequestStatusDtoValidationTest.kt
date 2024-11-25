package itmo.highload.model

import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.UpdateAdoptionRequestStatusDto
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.ConstraintViolation
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAdoptionRequestStatusDtoValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should fail validation when id is null`() {
        val updateDto = UpdateAdoptionRequestStatusDto(
            id = null,
            status = AdoptionStatus.APPROVED
        )

        val violations: Set<ConstraintViolation<UpdateAdoptionRequestStatusDto>> = validator.validate(updateDto)

        assertEquals(1, violations.size)
        val violation = violations.first()

        assertTrue(violation.constraintDescriptor.annotation is NotNull)
        assertEquals("must not be null", violation.message)
    }

    @Test
    fun `should fail validation when status is null`() {
        val updateDto = UpdateAdoptionRequestStatusDto(
            id = 1,
            status = null
        )

        val violations: Set<ConstraintViolation<UpdateAdoptionRequestStatusDto>> = validator.validate(updateDto)

        assertEquals(1, violations.size)
        val violation = violations.first()

        assertTrue(violation.constraintDescriptor.annotation is NotNull)
        assertEquals("must not be null", violation.message)
    }

    @Test
    fun `should pass validation when all fields are valid`() {
        val updateDto = UpdateAdoptionRequestStatusDto(
            id = 1,
            status = AdoptionStatus.APPROVED
        )

        val violations: Set<ConstraintViolation<UpdateAdoptionRequestStatusDto>> = validator.validate(updateDto)

        assertEquals(0, violations.size)
    }
}
