package itmo.highload


import itmo.highload.dto.RegisterDto
import itmo.highload.security.Role
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterDtoValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should fail validation when login is blank`() {
        val registerDto = RegisterDto(
            login = "",
            password = "validPassword123",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(2, violations.size)

        val notBlankViolation =
            violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "NotBlank" }
        assertTrue(notBlankViolation?.constraintDescriptor?.annotation is NotBlank)
        assertEquals("must not be empty", notBlankViolation?.message)
    }

    @Test
    fun `should fail validation when password is blank`() {
        val registerDto = RegisterDto(
            login = "validLogin",
            password = "",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(2, violations.size)

        val notBlankViolation =
            violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "NotBlank" }
        assertTrue(notBlankViolation?.constraintDescriptor?.annotation is NotBlank)
        assertEquals("must not be empty", notBlankViolation?.message)
    }

    @Test
    fun `should fail validation when login is too short`() {
        val registerDto = RegisterDto(
            login = "abc",
            password = "validPassword123",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(1, violations.size)

        val sizeViolation = violations.first()
        assertTrue(sizeViolation.constraintDescriptor.annotation is Size)
        assertEquals("size must be between 4 and 50", sizeViolation.message)
    }

    @Test
    fun `should fail validation when login contains invalid characters`() {
        val registerDto = RegisterDto(
            login = "invalid@login",
            password = "validPassword123",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(1, violations.size)

        val patternViolation = violations.first()
        assertTrue(patternViolation.constraintDescriptor.annotation is Pattern)
        assertEquals("must match \"^[a-zA-Z0-9_]*$\"", patternViolation.message)
    }

    @Test
    fun `should fail validation when password is too short`() {
        val registerDto = RegisterDto(
            login = "validLogin",
            password = "ab",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(1, violations.size)

        val sizeViolation = violations.first()
        assertTrue(sizeViolation.constraintDescriptor.annotation is Size)
        assertEquals("size must be between 3 and 50", sizeViolation.message)
    }

    @Test
    fun `should fail validation when password contains invalid characters`() {
        val registerDto = RegisterDto(
            login = "validLogin",
            password = "invalid@password",
            role = Role.CUSTOMER
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(1, violations.size)

        val patternViolation = violations.first()
        assertTrue(patternViolation.constraintDescriptor.annotation is Pattern)
        assertEquals("must match \"^[a-zA-Z0-9_]*$\"", patternViolation.message)
    }

    @Test
    fun `should fail validation when role is null`() {
        val registerDto = RegisterDto(
            login = "validLogin",
            password = "validPassword123",
            role = null
        )

        val violations: Set<ConstraintViolation<RegisterDto>> = validator.validate(registerDto)
        assertEquals(1, violations.size)

        val notNullViolation = violations.first()
        assertTrue(notNullViolation.constraintDescriptor.annotation is NotNull)
        assertEquals("must not be null", notNullViolation.message)
    }
}
