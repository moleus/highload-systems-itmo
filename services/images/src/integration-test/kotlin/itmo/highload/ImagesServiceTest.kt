// File: `src/test/kotlin/itmo/highload/ImagesControllerIntegrationTest.kt`
package itmo.highload

import io.restassured.RestAssured
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.model.ImageRef
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.withJwt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.junit.jupiter.Container

@R2dbcIntegrationTestContext
class ImagesServiceTest @Autowired constructor(
    jwtUtils: JwtUtils
) : TestContainerIntegrationTest() {
    companion object {
        @Container
        @JvmStatic
        val minio = MinIOContainer("minio/minio")

        @DynamicPropertySource
        @JvmStatic
        fun minioProps(registry: DynamicPropertyRegistry) {
            registry.add("minio.username") { minio.userName }
            registry.add("minio.password") { minio.password }
            registry.add("minio.url") { minio.s3URL }
            registry.add("minio.port") { minio.firstMappedPort }
            registry.add("minio.bucketName") { "images" }
            registry.add("minio.defaultFolder") { "" }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    private val customerToken = jwtUtils.generateAccessToken(
        "customer",
        Role.CUSTOMER,
        -2
    )

    @Test
    fun `test upload image`() {
        val response = RestAssured.given()
            .withJwt(customerToken)
            .port(port)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", ClassPathResource("test-image.png", ImagesServiceTest::class.java).file)
            .post("/api/v1/images")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .`as`(ImageRef::class.java)

        assertNotNull(response)
        assertNotNull(response.id)
        assertNotNull(response.url)
    }

    @Test
    fun `test get image by id`() {
        val imageRef = uploadTestImage()

        val retrievedImageRef = RestAssured.given()
            .withJwt(customerToken)
            .port(port)
            .get("/api/v1/images/{id}", imageRef.id)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .`as`(ImageRef::class.java)

        assertNotNull(retrievedImageRef)
        assertEquals(imageRef.id, retrievedImageRef?.id)
        assertEquals(imageRef.url, retrievedImageRef?.url)
    }

    private fun uploadTestImage(): ImageRef {
        return RestAssured.given()
            .port(port)
            .withJwt(customerToken)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", ClassPathResource("test-image.png", ImagesServiceTest::class.java).file)
            .post("/api/v1/images")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .`as`(ImageRef::class.java)
    }
}
