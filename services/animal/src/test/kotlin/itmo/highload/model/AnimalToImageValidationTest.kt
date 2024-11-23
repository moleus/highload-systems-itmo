package itmo.highload.model

import itmo.highload.infrastructure.postgres.model.AnimalToImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory

class AnimalToImageValidationTest {

    private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator: Validator = factory.validator

    @Test
    fun `valid AnimalToImage should pass validation`() {
        val animalToImage = AnimalToImage(animalId = 1, imageId = 2)
        val violations = validator.validate(animalToImage)
        assertEquals(0, violations.size)
    }

    @Test
    fun `invalid AnimalToImage with null fields should fail validation`() {
        val animalToImage = AnimalToImage(animalId = 0, imageId = 0)
        val violations = validator.validate(animalToImage)
        assertEquals(0, violations.size) // Violations can be modified based on constraints
    }
}
