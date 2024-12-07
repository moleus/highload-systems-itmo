package itmo.highload.model

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.domain.entity.AnimalEntity
import itmo.highload.domain.mapper.AnimalMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnimalMapperTest {

    @Test
    fun `toEntity should map AnimalDto to Animal`() {
        val dto = AnimalDto(
            name = "Buddy",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val entity = AnimalMapper.toJpaEntity(dto)

        assertEquals(dto.name, entity.name)
        assertEquals(dto.type, entity.typeOfAnimal)
        assertEquals(dto.gender, entity.gender)
        assertEquals(dto.isCastrated, entity.isCastrated)
        assertEquals(dto.healthStatus, entity.healthStatus)
    }

    @Test
    fun `toAnimalResponse should map Animal to AnimalResponse`() {
        val entity = AnimalEntity(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val response = AnimalMapper.toAnimalResponse(entity)

        assertEquals(entity.id, response.id)
        assertEquals(entity.name, response.name)
        assertEquals(entity.typeOfAnimal, response.type)
        assertEquals(entity.gender, response.gender)
        assertEquals(entity.isCastrated, response.isCastrated)
        assertEquals(entity.healthStatus, response.healthStatus)
    }
}
