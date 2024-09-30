package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import io.restassured.path.json.JsonPath
import io.restassured.response.*
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.LoginDto
import itmo.highload.dto.RegisterDto
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.dto.response.JwtResponse
import itmo.highload.mapper.AnimalMapper
import itmo.highload.mapper.UserMapper
import itmo.highload.model.AdoptionRequest
import itmo.highload.model.Animal
import itmo.highload.model.Customer
import itmo.highload.model.User
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.model.enum.Role
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.repository.UserRepository
import itmo.highload.service.UserService
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.domain.Pageable
import java.time.LocalDate
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

val users = listOf(
    User(
        login = "customer", password = "password", role = Role.CUSTOMER, creationDate = LocalDate.now()
    ), User(
        login = "customer2", password = "password", role = Role.CUSTOMER, creationDate = LocalDate.now()
    ), User(
        login = "expense-manager", password = "password", role = Role.EXPENSE_MANAGER, creationDate = LocalDate.now()
    ), User(
        login = "adoption-manager", password = "password", role = Role.ADOPTION_MANAGER, creationDate = LocalDate.now()
    )
)

val customers = users.mapIndexed { index, user ->
    Customer(
        gender = Gender.MALE, phone = "+7999123221${index}", address = "None Avenue"
    )
}

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,

    ) {
    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    private fun getToken(login: String): String {
        userRepository.findByLogin(login) ?: throw IllegalArgumentException("User not found")
        return defaultJsonRequestSpec().header("Content-type", "application/json").body(LoginDto(login, "password"))
            .post("/api/v1/auth/login").then().log().ifValidationFails(LogDetail.BODY).statusCode(200).extract().body()
            .`as`(JwtResponse::class.java).accessToken
    }

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON

        userRepository.deleteAll()
        users.map { u -> userService.addUser(RegisterDto(login = u.login, password = u.password, role = u.role)) }
        customerRepository.deleteAll()
        customerRepository.saveAllAndFlush(customers)
        animalRepository.deleteAll()
        animalRepository.saveAllAndFlush(animals)
        adoptionRequestRepository.deleteAll()
        animalRepository.findByName("Buddy", Pageable.unpaged()).map { animal ->
            AdoptionRequest(
                status = AdoptionStatus.PENDING,
                customer = customerRepository.findById(userRepository.findByLogin("customer")!!.id).get(),
                animal = animal,
                dateTime = LocalDateTime.now(),
                manager = null,
            )
        }.let { adoptionRequests -> adoptionRequestRepository.saveAllAndFlush(adoptionRequests) }
    }

    @Test
    @Order(1)
    fun `test add adoption request`() {
        val animal = animalRepository.findByName("Buddy", Pageable.unpaged()).first()
        val animalId = animal.id
        val user = userRepository.findByLogin("customer") ?: throw IllegalArgumentException("User not found")
        val token = getToken(user.login)

        val expectedMessage = "An adoption request already exists for customer ID: 1 and animal ID: 1"
        defaultJsonRequestSpec().withJwt(token).post("$apiUrlBasePath/$animalId").then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(400).body(`is`(expectedMessage))

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

        val response: Response = defaultJsonRequestSpec().withJwt(token).post("$apiUrlBasePath/${animal2.id}")

        val actualResponse = response.then().log().ifValidationFails(LogDetail.BODY).statusCode(201).extract().body()
            .`as`(AdoptionRequestResponse::class.java)
        Assertions.assertEquals(expectedAdoptionRequestResponse.id, actualResponse.id)
        Assertions.assertEquals(expectedAdoptionRequestResponse.status, actualResponse.status)
        Assertions.assertEquals(expectedAdoptionRequestResponse.customer, actualResponse.customer)
        Assertions.assertEquals(expectedAdoptionRequestResponse.animal, actualResponse.animal)
    }

    @Test
    @Order(2)
    fun `test get all adoption requests for customer`() {
        val user = users[0]
        val token = getToken(user.login)

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
            defaultJsonRequestSpec().withJwt(token).get(apiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(200).extract()
        val jsonPathEvaluator: JsonPath = response.jsonPath()
        val adoptions : List<AdoptionRequestResponse> = jsonPathEvaluator.getList("", AdoptionRequestResponse::class.java)
        Assertions.assertEquals(expectedAdoptionRequestResponse, adoptions)

        val user2 = users[1]
        val token2 = getToken(user2.login)
        val response2: List<*> =
            defaultJsonRequestSpec().withJwt(token2).get(apiUrlBasePath).then().log().ifValidationFails(LogDetail.BODY)
                .statusCode(200).extract().`as`(List::class.java)
        Assertions.assertTrue(response2.isEmpty())
    }

    @Test
    @Order(3)
    fun `test update adoption request`() {
        val adoptionRequest = adoptionRequestRepository.findAll()[0]
        val requestBody = UpdateAdoptionRequestStatusDto(
            id = adoptionRequest.id, status = AdoptionStatus.APPROVED
        )

        // Token for the owner of the adoption request
        val ownerToken = getToken(users[0].login)
        val ownerResponse: Response =
            defaultJsonRequestSpec().withJwt(ownerToken).body(requestBody).patch(apiUrlBasePath)
        ownerResponse.then().log().ifValidationFails(LogDetail.BODY).statusCode(403)

        // Token for the adoption manager
        val managerToken = getToken("adoption-manager")
        val managerResponse: Response =
            defaultJsonRequestSpec().withJwt(managerToken).body(requestBody).patch(apiUrlBasePath)
        managerResponse.then().log().ifValidationFails(LogDetail.BODY).statusCode(200)
            .body("status", equalTo(AdoptionStatus.APPROVED.name))
    }

    @Test
    @Order(4)
    fun `test only owner can delete adoption request`() {
        val animalId = animalRepository.findByName("Buddy", Pageable.unpaged()).first().id

        // Token for another customer
        val otherCustomerToken = getToken(users[1].login)
        val otherCustomerResponse: Response =
            defaultJsonRequestSpec().withJwt(otherCustomerToken).delete("$apiUrlBasePath/$animalId")
        otherCustomerResponse.then().log().ifValidationFails(LogDetail.BODY).statusCode(400)

        // Token for the owner of the adoption request
        val ownerToken = getToken(users[0].login)
        val ownerResponse: Response = defaultJsonRequestSpec().withJwt(ownerToken).delete("$apiUrlBasePath/$animalId")
        ownerResponse.then().log().ifValidationFails(LogDetail.BODY).statusCode(204)
    }

    @ParameterizedTest(name = "Only customer can delete adoption request: {0}")
    @MethodSource("provideLogins")
    fun `test only customer can delete adoption request`(login: String) {
        if (login.contains("customer")) {
            return
        }
        val animalId = animalRepository.findByName("Buddy", Pageable.unpaged()).first().id
        val response: Response = defaultJsonRequestSpec().delete("$apiUrlBasePath/$animalId")

        response.then().log().ifValidationFails(LogDetail.BODY)
            .statusCode(403) // Adjust based on expected status code for forbidden access
    }

    companion object {
        @JvmStatic
        fun provideLogins(): List<String> {
            return users.map { it.login }
        }
    }
}
