package itmo.highload

import io.restassured.RestAssured
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.configuration.R2dbcIntegrationTestContext
import itmo.highload.configuration.TestContainerIntegrationTest
import itmo.highload.security.Role
import itmo.highload.security.jwt.JwtUtils
import itmo.highload.utils.withJwt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
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
            registry.add("minio.publicEndpoint") { "http://localhost:${minio.firstMappedPort}" }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    private val customerToken = jwtUtils.generateAccessToken(
        "customer", Role.CUSTOMER, -2
    )

    private val managerToken = jwtUtils.generateAccessToken(
        "amanager", Role.ADOPTION_MANAGER, -3
    )

    @Test
    fun `test upload image`() {
        val response =
            RestAssured.given().withJwt(customerToken).port(port).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("file", ClassPathResource("/test-image.png", ImagesServiceTest::class.java).file)
                .post("/api/v1/images/upload").then().statusCode(HttpStatus.CREATED.value()).extract()
                .`as`(UploadedFileResponse::class.java)

        assertNotNull(response)
        assertNotNull(response.fileID)
    }

    @Test
    fun `test get image by id`() {
        val imageRef = uploadTestImage()

        val retrievedS3ObjectRef =
            RestAssured.given().withJwt(customerToken).port(port).get("/api/v1/images/{id}", imageRef.fileID).then()
                .statusCode(HttpStatus.OK.value()).extract().`as`(FileUrlResponse::class.java)

        val actualUrl = retrievedS3ObjectRef.url

        assertNotNull(retrievedS3ObjectRef)
        assertEquals(imageRef.fileID, retrievedS3ObjectRef?.fileID)
        assert(
            actualUrl.contains("http://localhost:${minio.firstMappedPort}/images/")
        ) { "Actual url is: $actualUrl" }

    }

    @Disabled
    @Test
    fun `test update image by id`() {
        val imageRef = uploadTestImage()

        val originalImageObjectContentsBytes = RestAssured.given().withJwt(customerToken).port(port).get("/api/v1/images/{id}", imageRef.fileID).then()
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(FileUrlResponse::class.java).url.toByteArray()

        val gotImageRef = RestAssured.given().withJwt(customerToken).port(port).get("/api/v1/images/{id}", imageRef.fileID).then()
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(FileUrlResponse::class.java)

        // convertion to ByteArray fails
        val gotImageObjectContentsBytes = RestAssured.given().withJwt(customerToken).get(gotImageRef.url).then()
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(ByteArray::class.java)

        assert(gotImageRef.url.contains("http://localhost:${minio.firstMappedPort}/images/")) { "Actual url is: ${gotImageRef.url}" }
        assertEquals(originalImageObjectContentsBytes.size, gotImageObjectContentsBytes.size)
        assert(originalImageObjectContentsBytes.contentEquals(gotImageObjectContentsBytes)) { "Original image contents: ${originalImageObjectContentsBytes.contentToString()}, got image contents: ${gotImageObjectContentsBytes.contentToString()}" }

        val updatedImageRef = RestAssured.given().withJwt(customerToken).port(port).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", ClassPathResource("/cute-cats.jpg", ImagesServiceTest::class.java).file)
            .put("/api/v1/images/{id}", imageRef.fileID).then()
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(FileUrlResponse::class.java)

        assertEquals(gotImageRef.url, updatedImageRef.url)
        assertEquals(gotImageRef.fileID, updatedImageRef.fileID)

        val updatedImageObjectContentsBytes = RestAssured.given().withJwt(customerToken).get(updatedImageRef.url).then()
            .statusCode(HttpStatus.OK.value())
            .extract().`as`(ByteArray::class.java)

        assertNotEquals(originalImageObjectContentsBytes.size, updatedImageObjectContentsBytes.size)
        assert(!originalImageObjectContentsBytes.contentEquals(updatedImageObjectContentsBytes)) { "Original image contents: ${originalImageObjectContentsBytes.contentToString()}, updated image contents: ${updatedImageObjectContentsBytes.contentToString()}" }
    }

    @Test
    fun `test delete image by id`() {
        val imageRef = uploadTestImage()

        RestAssured.given().withJwt(managerToken).port(port).delete("/api/v1/images/{id}", imageRef.fileID).then()
            .statusCode(HttpStatus.NO_CONTENT.value())

        RestAssured.given().withJwt(customerToken).port(port).get("/api/v1/images/{id}", imageRef.fileID).then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    private fun uploadTestImage(): UploadedFileResponse {
        return RestAssured.given().port(port).withJwt(customerToken).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", ClassPathResource("/test-image.png", ImagesServiceTest::class.java).file)
            .post("/api/v1/images/upload").then().statusCode(HttpStatus.CREATED.value()).extract()
            .`as`(UploadedFileResponse::class.java)
    }
}
