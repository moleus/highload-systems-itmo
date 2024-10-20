package itmo.highload

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnimalDtoValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should fail validation when name is blank`() {
        val animalDto = AnimalDto(
            name = "",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(2, violations.size)

        val notBlankViolation = violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "NotBlank" }
        assertEquals("не должно быть пустым", notBlankViolation?.message)

        val sizeViolation = violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "Size" }
        assertEquals("размер должен находиться в диапазоне от 1 до 50", sizeViolation?.message)
    }

    @Test
    fun `should fail validation when type is blank`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(2, violations.size)

        val notBlankViolation = violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "NotBlank" }
        assertEquals("не должно быть пустым", notBlankViolation?.message)

        val sizeViolation = violations.firstOrNull { it.constraintDescriptor.annotation.annotationClass.simpleName == "Size" }
        assertEquals("размер должен находиться в диапазоне от 1 до 50", sizeViolation?.message)
    }

    @Test
    fun `should fail validation when name is too long`() {
        val animalDto = AnimalDto(
            name = "a".repeat(51),
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(1, violations.size)
        assertEquals("размер должен находиться в диапазоне от 1 до 50", violations.first().message)
    }

    @Test
    fun `should fail validation when type is too long`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "a".repeat(51),
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(1, violations.size)
        assertEquals("размер должен находиться в диапазоне от 1 до 50", violations.first().message)
    }

    @Test
    fun `should fail validation when gender is null`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = null,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(1, violations.size)
        assertEquals("не должно равняться null", violations.first().message)
    }

    @Test
    fun `should fail validation when isCastrated is null`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = null,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(1, violations.size)
        assertEquals("не должно равняться null", violations.first().message)
    }

    @Test
    fun `should fail validation when healthStatus is null`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = null
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(1, violations.size)
        assertEquals("не должно равняться null", violations.first().message)
    }

    @Test
    fun `should pass validation when all fields are valid`() {
        val animalDto = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val violations: Set<ConstraintViolation<AnimalDto>> = validator.validate(animalDto)
        assertEquals(0, violations.size)
    }

}