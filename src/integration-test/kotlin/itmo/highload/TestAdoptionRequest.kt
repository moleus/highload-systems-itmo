package itmo.highload

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import io.restassured.response.Response
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.dto.response.UserResponse
import itmo.highload.model.*
import itmo.highload.model.enum.AdoptionStatus
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.model.enum.UserRole
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.CustomerRepository
import itmo.highload.repository.UserRepository
import itmo.highload.security.jwt.JwtProvider
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


val animals = listOf(
    Animal(
        id = 1000,
        name = "Buddy",
        typeOfAnimal = "Dog",
        gender = Gender.MALE,
        isCastrated = true,
        healthStatus = HealthStatus.HEALTHY
    ), Animal(
        id = 1,
        name = "Molly",
        typeOfAnimal = "Cat",
        gender = Gender.FEMALE,
        isCastrated = false,
        healthStatus = HealthStatus.SICK
    )
)

val users = listOf(
    User(
        id = 1000, login = "customer", password = "password", role = UserRole.CUSTOMER, creationDate = LocalDate.now()
    ), User(
        id = 1001, login = "customer2", password = "password", role = UserRole.CUSTOMER, creationDate = LocalDate.now()
    )
)

val customers = users.map {
    Customer(
        id = it.id, gender = Gender.MALE, phone = "+79991234567", address = "None Avenue"
    )
}

val adoptionRequests = listOf(
    AdoptionRequest(
        id = 1000,
        status = AdoptionStatus.PENDING,
        customer = customers[0],
        animal = animals[0],
        dateTime = LocalDateTime.now(),
        manager = null,
    )
)

//@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(value = ["classpath:application-test.properties"] )
@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
    private val animalRepository: AnimalRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
) {
    companion object {
//        @Container
//        @JvmStatic
//        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15"))
//            .withUsername("test").withPassword("test").withDatabaseName("test")

//        @DynamicPropertySource
//        @JvmStatic
//        fun postgresProperties(registry: DynamicPropertyRegistry) {
//            println("JDBC URL: ${postgres.jdbcUrl}")
////            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
//            registry.add("spring.datasource.url", postgres::getJdbcUrl)
//            registry.add("spring.datasource.username", postgres::getUsername)
//            registry.add("spring.datasource.password", postgres::getPassword)
//        }
//
//        @BeforeAll
//        @JvmStatic
//        fun pgStart() {
//            postgres.setWaitStrategy(
//                LogMessageWaitStrategy()
//                    .withRegEx(".*database system is ready to accept connections.*\\s")
//                    .withTimes(1)
//                    .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
//            )
//            postgres.start()
//        }

//        @AfterAll
//        @JvmStatic
//        fun pgStop() {
//            postgres.stop()
//        }
    }

    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoption-requests"

    private fun getCustomerToken(login: String) = jwtProvider.generateAccessToken(login, UserRole.CUSTOMER)

    private fun getAdoptionManagerToken(login: String) =
        jwtProvider.generateAccessToken(login, UserRole.ADOPTION_MANAGER)


    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON;

        userRepository.deleteAll()
        userRepository.saveAll(users)
        customerRepository.deleteAll()
        customerRepository.saveAll(customers)
        adoptionRequestRepository.deleteAll()
        adoptionRequestRepository.saveAll(adoptionRequests)
        animalRepository.deleteAll()
        animalRepository.saveAll(animals)
    }

    @Test
    fun `test add adoption request`() {
        val animal = animals[0]
        val animalId = animal.id
        val user = users[0]
        val token = getCustomerToken(user.login)

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
            customer = UserResponse(id = user.id, login = "customer"),
            manager = null,
            animal = expectedAnimalResponse
        )

        val response: Response = defaultJsonRequestSpec().withJwt(token).post("$apiUrlBasePath/$animalId")

        response.then().statusCode(200).body("animal", equalTo(expectedAdoptionRequestResponse))
    }

    @Test
    fun `test get all adoption requests for customer`() {
        val user = users[0]
        val token = getCustomerToken(user.login)

        val expectedAdoptionRequestResponse = adoptionRequests.map {
            AdoptionRequestResponse(
                id = it.id,
                dateTime = it.dateTime,
                status = it.status,
                customer = UserResponse(id = user.id, login = "customer"),
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
        val token2 = getCustomerToken(user2.login)
        val response2: Response = defaultJsonRequestSpec().withJwt(token2).get(apiUrlBasePath)
        response2.then().statusCode(200).body("", `is`(emptyList<AdoptionRequestResponse>()))
    }

    @Test
    fun `test update adoption request`() {
        val requestBody = UpdateAdoptionRequestStatusDto(
            id = 1, status = AdoptionStatus.APPROVED
        )

        // Token for the owner of the adoption request
        val ownerToken = getCustomerToken(users[0].login)
        val ownerResponse: Response =
            defaultJsonRequestSpec().withJwt(ownerToken).body(requestBody).patch(apiUrlBasePath)
        ownerResponse.then().statusCode(403)

        // Token for the adoption manager
        val managerToken = getAdoptionManagerToken("manager")
        val managerResponse: Response =
            defaultJsonRequestSpec().withJwt(managerToken).body(requestBody).patch(apiUrlBasePath)
        managerResponse.then().statusCode(200).body("status", equalTo(AdoptionStatus.APPROVED.name))
    }

    @Test
    fun `test only owner can delete adoption request`() {
        val animalId = animals[0].id

        // Token for another customer
        val otherCustomerToken = getCustomerToken(users[1].login)
        val otherCustomerResponse: Response =
            defaultJsonRequestSpec().withJwt(otherCustomerToken).delete("$apiUrlBasePath/$animalId")
        otherCustomerResponse.then().statusCode(403)

        // Token for the owner of the adoption request
        val ownerToken = getCustomerToken(users[0].login)
        val ownerResponse: Response = defaultJsonRequestSpec().withJwt(ownerToken).delete("$apiUrlBasePath/$animalId")
        ownerResponse.then().statusCode(200)
    }

    @ParameterizedTest(name = "Only customer can delete adoption request: {0}")
    @EnumSource(UserRole::class)
    fun `test only customer can delete adoption request`(role: UserRole) {
        if (role == UserRole.CUSTOMER) {
            return
        }
        val animalId = 1
        val otherRoleToken = jwtProvider.generateAccessToken("customer", role)
        val response: Response = defaultJsonRequestSpec().withJwt(otherRoleToken).delete("$apiUrlBasePath/$animalId")

        response.then().statusCode(403) // Adjust based on expected status code for forbidden access
    }
}

