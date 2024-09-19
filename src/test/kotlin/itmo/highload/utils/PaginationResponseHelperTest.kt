package itmo.highload.utils

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity

class PaginationResponseHelperTest {

    private val paginationResponseHelper = PaginationResponseHelper()

    @Test
    fun `test createInfinityScrollHeaders`() {
        val page = mockk<Page<*>>()
        every { page.number } returns 1
        every { page.hasNext() } returns true

        val headers: HttpHeaders = paginationResponseHelper.createInfinityScrollHeaders(page)

        assertEquals("1", headers["X-Current-Page"]?.get(0))
        assertEquals("true", headers["X-Has-Next-Page"]?.get(0))
    }

    @Test
    fun `test createPaginationHeaders`() {
        val page = mockk<Page<*>>()
        every { page.number } returns 2
        every { page.totalPages } returns 10
        every { page.totalElements } returns 100L
        every { page.numberOfElements } returns 50

        val headers: HttpHeaders = paginationResponseHelper.createPaginationHeaders(page)

        assertEquals("2", headers["X-Current-Page"]?.get(0))
        assertEquals("10", headers["X-Total-Pages"]?.get(0))
        assertEquals("100", headers["X-Total-Elements"]?.get(0))
        assertEquals("50", headers["X-Current-Element-Num"]?.get(0))
    }


    @Test
    fun `test createPaginatedResponse`() {
        val entityPage = mockk<Page<String>>()
        val dataTransformer: (List<String>) -> List<Int> = { content -> content.map { it.length } }

        every { entityPage.content } returns listOf("Item1", "Item22", "Item333")
        every { entityPage.number } returns 0
        every { entityPage.totalPages } returns 1
        every { entityPage.totalElements } returns 3
        every { entityPage.numberOfElements } returns 3

        val response: ResponseEntity<List<Int>> = paginationResponseHelper.createPaginatedResponse(
            entityPage, dataTransformer)

        val expectedTransformedData = listOf(5, 6, 7)
        val headers = response.headers

        assertEquals("0", headers["X-Current-Page"]?.first())
        assertEquals("1", headers["X-Total-Pages"]?.first())
        assertEquals("3", headers["X-Total-Elements"]?.first())
        assertEquals("3", headers["X-Current-Element-Num"]?.first())

        assertEquals(expectedTransformedData, response.body)

    }
}
