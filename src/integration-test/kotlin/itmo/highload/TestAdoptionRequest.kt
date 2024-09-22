package itmo.highload

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.model.Animal
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort

fun defaultJsonRequestSpec(): RequestSpecification = RestAssured.given().contentType(ContentType.JSON)

@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
) {

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    val animals = listOf(
        Animal(
            id = 1,
            name = "Buddy",
            typeOfAnimal = "Dog",
            gender = Gender.MALE,
            isCastrated = true,
            healthStatus = HealthStatus.HEALTHY
        ), Animal(
            id = 2,
            name = "Molly",
            typeOfAnimal = "Cat",
            gender = Gender.FEMALE,
            isCastrated = false,
            healthStatus = HealthStatus.SICK
        )
    )

    @BeforeEach
    fun setUp() {
        RestAssured.port = port

        adoptionRequestRepository.deleteAll()
        animalRepository.deleteAll()
        animalRepository.saveAll(animals)
    }

    @Test
    fun `test add adoption request`() {
        val animalId = animals[0].id
        val response: Response = defaultJsonRequestSpec().post("$apiUrlBasePath/$animalId")

        response.then().statusCode(200).body("animalId", equalTo(animalId))
    }

    @Test
    fun `test get all adoption requests`() {
        val response: Response = defaultJsonRequestSpec().get(apiUrlBasePath)

        response.then().statusCode(200).body("size()", equalTo(0)) // Adjust based on expected data
    }

    @Test
    fun `test update adoption request`() {
        val requestBody = mapOf("status" to "APPROVED")
        val response: Response = defaultJsonRequestSpec().body(requestBody).patch(apiUrlBasePath)

        // expect unauthorized
        response.then().statusCode(401)
    }

    @Test
    fun `test delete adoption request`() {
        val animalId = 1
        val response: Response = defaultJsonRequestSpec().delete("$apiUrlBasePath/$animalId")

        response.then().statusCode(200)
    }
}

