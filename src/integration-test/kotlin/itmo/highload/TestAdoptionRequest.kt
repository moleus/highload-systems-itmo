package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.mapper.AnimalMapper
import itmo.highload.mapper.UserMapper
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.Animal
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.repository.UserRepository
import itmo.highload.service.DEMO_CUSTOMER_LOGIN
import itmo.highload.utils.defaultJsonRequestSpec
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

val animals = listOf(
    Animal(
        name = "Buddy",
        typeOfAnimal = "Dog",
        gender = Gender.MALE,
        isCastrated = true,
        healthStatus = HealthStatus.HEALTHY
    ), Animal(
        name = "Molly",
        typeOfAnimal = "Cat",
        gender = Gender.FEMALE,
        isCastrated = false,
        healthStatus = HealthStatus.SICK
    )
)

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
) {
    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON

        animalRepository.deleteAll()
        animalRepository.saveAllAndFlush(animals)
        adoptionRequestRepository.deleteAll()
        animalRepository.findByName("Buddy", Pageable.unpaged()).map { animal ->
            AdoptionRequest(
                status = AdoptionStatus.PENDING,
                customer = customerRepository.findById(userRepository.findByLogin(DEMO_CUSTOMER_LOGIN)!!.id).get(),
                animal = animal,
                dateTime = LocalDateTime.now(),
                manager = null,
            )
        }.let { adoptionRequests -> adoptionRequestRepository.saveAllAndFlush(adoptionRequests) }
    }

    @Test
    fun `test add adoption request`() {
        val animal = animalRepository.findByName("Buddy", Pageable.unpaged()).first()
        val animalId = animal.id
        val user = userRepository.findByLogin(DEMO_CUSTOMER_LOGIN) ?: throw IllegalArgumentException("User not found")

        val expectedMessage = "An adoption request already exists for customer ID: 1 and animal ID: 1"
        defaultJsonRequestSpec().post("$apiUrlBasePath/$animalId").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.BAD_REQUEST.value()).body(`is`(expectedMessage))

        val animal2 = animalRepository.findByName("Molly", Pageable.unpaged()).first()

        val expectedAnimalResponse = AnimalMapper.toAnimalResponse(animal2)
        val expectedAdoptionRequestResponse = AdoptionRequestResponse(
            id = 2,
            dateTime = LocalDateTime.now(),
            status = AdoptionStatus.PENDING,
            customer = UserMapper.toResponse(customerRepository.findById(user.id).get()),
            animal = expectedAnimalResponse,
            manager = null
        )

        val response: Response = defaultJsonRequestSpec().post("$apiUrlBasePath/${animal2.id}")

        val actualResponse =
            response.then().log().ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.CREATED.value()).extract()
                .body()
                .`as`(AdoptionRequestResponse::class.java)
        Assertions.assertEquals(expectedAdoptionRequestResponse.id, actualResponse.id)
        Assertions.assertEquals(expectedAdoptionRequestResponse.status, actualResponse.status)
        Assertions.assertEquals(expectedAdoptionRequestResponse.customer, actualResponse.customer)
        Assertions.assertEquals(expectedAdoptionRequestResponse.animal, actualResponse.animal)
    }

    @Test
    fun `test get all adoption requests for customer`() {
        val expectedAdoptionRequestResponse = adoptionRequestRepository.findAll().map {
            AdoptionRequestResponse(
                id = it.id,
                dateTime = it.dateTime,
                status = it.status,
                customer = UserMapper.toResponse(it.customer),
                manager = null,
                animal = AnimalMapper.toAnimalResponse(it.animal)
            )
        }

        val response =
            defaultJsonRequestSpec().get(apiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(HttpStatus.OK.value()).extract()
        val jsonPathEvaluator: JsonPath = response.jsonPath()
        val adoptions: List<AdoptionRequestResponse> =
            jsonPathEvaluator.getList("", AdoptionRequestResponse::class.java)
        Assertions.assertEquals(expectedAdoptionRequestResponse, adoptions)
    }
}
