package itmo.highload

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.model.Animal
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.model.enum.UserRole
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort


@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
    private val jwtProvider: JwtProvider,
) {

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    private val customerToken = jwtProvider.generateAccessToken("customer", UserRole.CUSTOMER)

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
        RestAssured.defaultParser = Parser.JSON;

        adoptionRequestRepository.deleteAll()
        animalRepository.deleteAll()
        animalRepository.saveAll(animals)
    }

    @Test
    fun `test add adoption request`() {
        val animal = animals[0]
        val animalId = animal.id
        val response: Response = defaultJsonRequestSpec().withJwt(customerToken).post("$apiUrlBasePath/$animalId")

        val expectedAnimalResponse = animal.let {
            AnimalResponse(
                id = it.id,
                name = it.name,
                type = it.typeOfAnimal,
                gender = it.gender,
                isCastrated = it.isCastrated,
                healthStatus = it.healthStatus
            )
        }

        response.then().statusCode(200).body("animal", equalTo(expectedAnimalResponse))
    }

    @Test
    fun `test get all adoption requests`() {
        val response: Response = defaultJsonRequestSpec().withJwt(customerToken).get(apiUrlBasePath)

        response.then().statusCode(200).body("size()", equalTo(0)) // Adjust based on expected data
    }

    @Test
    fun `test update adoption request`() {
        val requestBody = mapOf("status" to "APPROVED")
        val response: Response = defaultJsonRequestSpec().withJwt(customerToken).body(requestBody).patch(apiUrlBasePath)

        response.then().statusCode(200).body("status", equalTo("APPROVED"))
    }

    @Test
    fun `test delete adoption request`() {
        val animalId = 1
        val response: Response = defaultJsonRequestSpec().withJwt(customerToken).delete("$apiUrlBasePath/$animalId")

        response.then().statusCode(200)
    }

    @ParameterizedTest(name = "Only customer can delete adoption request: {0}")
    @EnumSource(UserRole::class)
    fun `test only customer can delete adoption request`(role: UserRole) {
        if (role == UserRole.CUSTOMER) {
            return
        }
        val animalId = 1
        val otherRoleToken = jwtProvider.generateAccessToken("otherRoleUser", role)
        val response: Response = defaultJsonRequestSpec().withJwt(otherRoleToken).delete("$apiUrlBasePath/$animalId")

        response.then().statusCode(403) // Adjust based on expected status code for forbidden access
    }
}

