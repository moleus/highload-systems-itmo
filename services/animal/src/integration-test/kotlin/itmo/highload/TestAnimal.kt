package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.model.AnimalMapper
import itmo.highload.repository.AnimalRepository
import itmo.highload.utils.defaultJsonRequestSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@IntegrationTestContext
class TestAnimal @Autowired constructor(
    private val animalRepository: AnimalRepository,
//    jwtUtils: JwtUtils,
) {
    @LocalServerPort
    private var port: Int = 0
    private val animalApiUrlBasePath = "/api/v1/animals"

//    private val customerToken = jwtUtils.generateAccessToken("customer", Role.CUSTOMER, 1)
//    private val adoptionManagerToken = jwtUtils.generateAccessToken("adoption_manager", Role.ADOPTION_MANAGER, 1)

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `test get all animals`() {
        val expectedAnimalResponse = animalRepository.findAll().map {
            AnimalMapper.toAnimalResponse(it)
        }

        val actualAnimalResponse =
            defaultJsonRequestSpec().get(animalApiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract().`as`(Array<AnimalResponse>::class.java).toList()

        assertThat(actualAnimalResponse).containsExactlyInAnyOrderElementsOf(expectedAnimalResponse)
    }

    @Test
    fun `test get animal by id`() {
        val animal = animalRepository.findAll().first()
        val expectedAnimalResponse = AnimalMapper.toAnimalResponse(animal)

        val actualAnimalResponse = defaultJsonRequestSpec().get("$animalApiUrlBasePath/${animal.id}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract()
            .`as`(AnimalResponse::class.java)

        assertThat(actualAnimalResponse).isEqualTo(expectedAnimalResponse)
    }

    @Test
    fun `test add animal`() {
        val animalDto = AnimalDto(
            name = "New Animal",
            type = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        )

        val response: Response = defaultJsonRequestSpec().body(animalDto).post(animalApiUrlBasePath)

        response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo(animalDto.name))

        val createdAnimal = animalRepository.findAll().find { it.name == animalDto.name }
        assertThat(createdAnimal).isNotNull
    }

    @Test
    fun `test update animal`() {
        val animal = animalRepository.findAll().first()
        val updatedAnimalDto = AnimalDto(
            name = "Updated Animal",
            type = animal.typeOfAnimal,
            gender = animal.gender,
            isCastrated = animal.isCastrated,
            healthStatus = animal.healthStatus
        )

        defaultJsonRequestSpec().body(updatedAnimalDto).put("$animalApiUrlBasePath/${animal.id}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value())
            .body("name", equalTo(updatedAnimalDto.name))

        val updatedAnimal = animalRepository.findById(animal.id).get()
        assertThat(updatedAnimal.name).isEqualTo(updatedAnimalDto.name)
    }

    @Test
    fun `test delete animal`() {
        val animal = animalRepository.findAll().first()

        defaultJsonRequestSpec().delete("$animalApiUrlBasePath/${animal.id}").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.NO_CONTENT.value())

        val deletedAnimal = animalRepository.findById(animal.id)
        assertThat(deletedAnimal).isEmpty
    }
}
