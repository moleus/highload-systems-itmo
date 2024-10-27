package itmo.highload

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.MultiValueMap
import java.io.IOException


class DemoApplicationTests {
    private lateinit var client: WebTestClient

    private val customerToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE3ODQwMjk4NzgsInN1YiI6ImN1c3RvbWVyIiwiaWF0IjoxNzMwMDI5ODc4LCJyb2xlIjoiQ1VTVE9NRVIiLCJ1c2VySWQiOjJ9.5mYn2BKVDwekryP-xlZV51SW-VEdBNwORUCWHiBjrPwIhMdFm1ZeGaIICYC7lweRKO-31Cyn2HqyUPdHaQ6VGQ"

    @BeforeEach
    fun setup() {
        this.client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + 8089)
            .build()
    }

    private fun generateBody(): MultiValueMap<String, HttpEntity<*>> {
        val builder = MultipartBodyBuilder()
        builder.part("file", ClassPathResource("/test-image.png", DemoApplicationTests::class.java))
        return builder.build()
    }

    @Test
    @Throws(IOException::class)
    fun testUpload() {
        val result = client
            .post()
            .uri("/api/v1/images/upload")
            .headers { headers -> headers.setBearerAuth(customerToken) }
            .bodyValue(generateBody())
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult().responseBody

        val objectMapper = ObjectMapper()
        val bodyMap: Map<*, *> = objectMapper.readValue(result, MutableMap::class.java)

        val fileId = bodyMap["id"] as String?
        assertNotNull(fileId)

        client
            .get()
            .uri("/api/v1/images/{id}", fileId)
            .headers { headers -> headers.setBearerAuth(customerToken) }
            .exchange()
            .expectStatus().isOk()
    }
}
