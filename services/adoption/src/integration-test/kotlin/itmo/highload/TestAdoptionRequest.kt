package itmo.highload

import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.parsing.Parser
import itmo.highload.api.dto.AdoptionStatus
import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.configuration.IntegrationTestContext
import itmo.highload.repository.AdoptionRequestRepository
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.defaultJsonRequestSpec
import itmo.highload.utils.withJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@IntegrationTestContext
class TestAdoptionRequest @Autowired constructor(
    private val adoptionRequestRepository: AdoptionRequestRepository,
//    private val animalRepository: AnimalRepository,
//    private val userRepository: UserRepository,
    private val jwtUtils: JwtUtils,
) {
    @LocalServerPort
    private var port: Int = 0
    private val apiUrlBasePath = "/api/v1/adoptions"
    private val adoptionManagerToken = jwtUtils.generateAccessToken(
        "adoption_manager",
        Role.ADOPTION_MANAGER,
        1
//        userRepository.findByLogin("amanager")?.id ?: throw IllegalArgumentException("User not found")
    )

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

//    @Test
//    fun `test add adoption request`() {
////        val userId = userRepository.findByLogin("customer")?.id ?: throw IllegalArgumentException("User not found")
//        val customerToken = this.jwtUtils.generateAccessToken(
//            "customer", Role.CUSTOMER, userId
//        )
//
////        val animal2 = animalRepository.findByName("Molly", Pageable.unpaged()).first()
//
//        val expectedAdoptionRequestResponse = AdoptionRequestResponse(
//            id = -1,
//            dateTime = LocalDateTime.now(),
//            status = AdoptionStatus.PENDING,
//            customerId = userId,
//            animalId = animal2.id,
//            managerId = null
//        )
//
//        val expectedStatusCode = HttpStatus.CREATED
//        val actualResponse =
//            defaultJsonRequestSpec().withJwt(customerToken).post("$apiUrlBasePath/${animal2.id}").then().log()
//                .ifValidationFails(LogDetail.BODY).statusCode(expectedStatusCode.value()).extract().body()
//                .`as`(AdoptionRequestResponse::class.java)
//        Assertions.assertEquals(expectedAdoptionRequestResponse.status, actualResponse.status)
//        Assertions.assertEquals(expectedAdoptionRequestResponse.customerId, actualResponse.customerId)
//        Assertions.assertEquals(expectedAdoptionRequestResponse.animalId, actualResponse.animalId)
//
//        val expectedMessage = "An adoption request already exists for customer ID: $userId and animal ID: ${animal2.id}"
//        val actualResponse2 =
//            defaultJsonRequestSpec().withJwt(customerToken).post("$apiUrlBasePath/${animal2.id}").then().log()
//                .ifValidationFails(LogDetail.BODY)
//        actualResponse2.statusCode(HttpStatus.BAD_REQUEST.value()).body(`is`(expectedMessage))
//    }

    @Test
    fun `test get all adoption requests for customer`() {
        val expectedAdoptionRequestResponse = adoptionRequestRepository.findAll().map {
            AdoptionRequestResponse(
                id = it.id,
                dateTime = it.dateTime,
                status = it.status,
                customerId = it.customerId,
                managerId = null,
                animalId = it.animalId,
            )
        }

        val result = defaultJsonRequestSpec().withJwt(adoptionManagerToken).get(apiUrlBasePath).then().log()
            .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract().body()
            .`as`(Array<AdoptionRequestResponse>::class.java).toList()
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedAdoptionRequestResponse)
    }

    @Test
    fun `should return list of statuses`() {
        val expectedStatuses = listOf(AdoptionStatus.PENDING, AdoptionStatus.APPROVED, AdoptionStatus.DENIED)
        val response =
            defaultJsonRequestSpec().withJwt(adoptionManagerToken).get("$apiUrlBasePath/statuses").then().log()
                .ifValidationFails(LogDetail.BODY).statusCode(HttpStatus.OK.value()).extract().body()
                .`as`(Array<AdoptionStatus>::class.java).toList()

        assertThat(response).containsExactlyInAnyOrderElementsOf(expectedStatuses)
    }
}
