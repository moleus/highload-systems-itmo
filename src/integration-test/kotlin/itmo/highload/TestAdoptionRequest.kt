package itmo.highload

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.LoginDto
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.dto.response.CustomerResponse
import itmo.highload.dto.response.JwtResponse
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
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.domain.Pageable
import org.springframework.test.context.TestPropertySource
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

val adoptionRequests = listOf(
    AdoptionRequest(
        status = AdoptionStatus.PENDING,
        customer = customers[0],
        animal = animals[0],
        dateTime = LocalDateTime.now(),
        manager = null,
    )
)

//@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestPropertySource(value = ["classpath:application-test.properties"])
@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
) {
    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    private fun getToken(login: String): String {
        val user = userRepository.findByLogin(login) ?: throw IllegalArgumentException("User not found")
        return defaultJsonRequestSpec()
            .header("Content-type", "application/json")
            .body(LoginDto(login, "password"))
            .post("/api/v1/auth/login")
            .then()
            .statusCode(200)
            .extract().body().`as`(JwtResponse::class.java).accessToken
    }

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON;

        userRepository.deleteAll()
        userRepository.saveAllAndFlush(users)
        customerRepository.deleteAll()
        customerRepository.saveAllAndFlush(customers)
        animalRepository.deleteAll()
        animalRepository.saveAllAndFlush(animals)
        adoptionRequestRepository.deleteAll()
        adoptionRequestRepository.saveAllAndFlush(adoptionRequests)
    }

    @Test
    @Order(1)
    fun `test add adoption request`() {
        val animal = animals[0]
        val animalId = animal.id
        val user = users[0]
        val token = getToken(user.login)

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
        val expectedAdoptionRequestResponse = AdoptionRequestResponse(
            id = 2,
            dateTime = LocalDateTime.now(),  // TODO: ignore time, we can't predict it
            status = AdoptionStatus.PENDING,
            customer = CustomerResponse(
                id = user.id,
                phone = "+799912345",
                gender = Gender.MALE,
                address = "None Avenue"
            ),
            manager = null,
            animal = expectedAnimalResponse
        )

        val response: Response = defaultJsonRequestSpec().withJwt(token).post("$apiUrlBasePath/$animalId")

        response.then().statusCode(200).body("animal", equalTo(expectedAdoptionRequestResponse))
    }

    @Test
    @Order(2)
    fun `test get all adoption requests for customer`() {
        val user = users[0]
        val token = getToken(user.login)

        val expectedAdoptionRequestResponse = adoptionRequests.map {
            AdoptionRequestResponse(
                id = it.id,
                dateTime = it.dateTime,
                status = it.status,
                customer = CustomerResponse(
                    id = it.customer.id,
                    phone = it.customer.phone,
                    gender = it.customer.gender,
                    address = it.customer.address
                ),
                manager = null,
                animal = AnimalResponse(
                    id = it.animal.id,
                    name = it.animal.name,
                    type = it.animal.typeOfAnimal,
                    gender = it.animal.gender,
                    isCastrated = it.animal.isCastrated,
                    healthStatus = it.animal.healthStatus
                )
            )
        }

        val response: Response = defaultJsonRequestSpec().withJwt(token).get(apiUrlBasePath)
        response.then().statusCode(200).body("", `is`(expectedAdoptionRequestResponse))

        // empty response for customer2

        val user2 = users[1]
        val token2 = getToken(user2.login)
        val response2: Response = defaultJsonRequestSpec().withJwt(token2).get(apiUrlBasePath)
        response2.then().statusCode(200).body("", `is`(emptyList<AdoptionRequestResponse>()))
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
        ownerResponse.then().statusCode(403)

        // Token for the adoption manager
        val managerToken = getToken("adoption-manager")
        val managerResponse: Response =
            defaultJsonRequestSpec().withJwt(managerToken).body(requestBody).patch(apiUrlBasePath)
        managerResponse.then().statusCode(200).body("status", equalTo(AdoptionStatus.APPROVED.name))
    }

    @Test
    @Order(4)
    fun `test only owner can delete adoption request`() {
        val animalId = animals[0].id

        // Token for another customer
        val otherCustomerToken = getToken(users[1].login)
        val otherCustomerResponse: Response =
            defaultJsonRequestSpec().withJwt(otherCustomerToken).delete("$apiUrlBasePath/$animalId")
        otherCustomerResponse.then().statusCode(403)

        // Token for the owner of the adoption request
        val ownerToken = getToken(users[0].login)
        val ownerResponse: Response = defaultJsonRequestSpec().withJwt(ownerToken).delete("$apiUrlBasePath/$animalId")
        ownerResponse.then().statusCode(200)
    }

    @ParameterizedTest(name = "Only customer can delete adoption request: {0}")
    @EnumSource(Role::class)
    fun `test only customer can delete adoption request`(role: Role) {
        if (role == Role.CUSTOMER) {
            return
        }
        val animalId = animalRepository.findByName("Buddy", Pageable.unpaged()).first().id
        val otherRoleToken = jwtProvider.generateAccessToken("customer", role)
        val response: Response = defaultJsonRequestSpec().withJwt(otherRoleToken).delete("$apiUrlBasePath/$animalId")

        response.then().statusCode(403) // Adjust based on expected status code for forbidden access
    }
}

