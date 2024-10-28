package itmo.highload

import io.restassured.RestAssured
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.model.S3ObjectRef
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
            .multiPart("file", ClassPathResource("/test-image.png", ImagesServiceTest::class.java).file)
            .post("/api/v1/images/upload")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .`as`(S3ObjectRef::class.java)

        assertNotNull(response)
        assertNotNull(response.id)
        assertNotNull(response.url)
    }

    @Test
    fun `test get image by id`() {
        val imageRef = uploadTestImage()

        val retrievedS3ObjectRef = RestAssured.given()
            .withJwt(customerToken)
            .port(port)
            .get("/api/v1/images/{id}", imageRef.fileID)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .`as`(FileUrlResponse::class.java)

        assertNotNull(retrievedS3ObjectRef)
        assertEquals(imageRef.fileID, retrievedS3ObjectRef?.fileID)
        assert(retrievedS3ObjectRef?.url?.contains("http://localhost:$port/images/") == true)
    }

    @Test
    fun `test delete image by id`() {
        val imageRef = uploadTestImage()

        RestAssured.given()
            .withJwt(customerToken)
            .port(port)
            .delete("/api/v1/images/{id}", imageRef.fileID)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())

        RestAssured.given()
            .withJwt(customerToken)
            .port(port)
            .get("/api/v1/images/{id}", imageRef.fileID)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    private fun uploadTestImage(): UploadedFileResponse {
        return RestAssured.given()
            .port(port)
            .withJwt(customerToken)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", ClassPathResource("/test-image.png", ImagesServiceTest::class.java).file)
            .post("/api/v1/images/upload")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .`as`(UploadedFileResponse::class.java)
    }
}
