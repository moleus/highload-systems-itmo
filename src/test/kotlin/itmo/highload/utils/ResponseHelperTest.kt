package itmo.highload.utils

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity

class ResponseHelperTest {

    private val paginationHeadersCreator = mockk<PaginationHeadersCreator>()
    private val responseHelper = ResponseHelper()

    @Test
    fun `test createPaginatedResponse`() {
        val page = mockk<Page<String>>()
        val contentList = listOf("Item1", "Item2", "Item3")
        every { page.content } returns contentList

        val headers = HttpHeaders()
        headers.add("X-Current-Page", "1")
        every { paginationHeadersCreator.createPaginationHeaders(page) } returns headers

        val dataTransformer: (List<String>) -> List<Int> = { list -> list.map { it.length } }

        val response: ResponseEntity<List<Int>> = responseHelper.createPaginatedResponse(
            page,
            dataTransformer, paginationHeadersCreator
        )

        assertEquals(3, response.body!!.size)
        assertEquals(5, response.body!![0])
        assertEquals("1", response.headers["X-Current-Page"]?.get(0))
    }
}
