package itmo.highload.utils

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders

class PaginationHeadersCreatorTest {

    private val paginationHeadersCreator = PaginationHeadersCreator()

    @Test
    fun `test createInfinityScrollHeaders`() {
        val page = mockk<Page<*>>()
        every { page.number } returns 1
        every { page.hasNext() } returns true

        val headers: HttpHeaders = paginationHeadersCreator.createInfinityScrollHeaders(page)

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

        val headers: HttpHeaders = paginationHeadersCreator.createPaginationHeaders(page)

        assertEquals("2", headers["X-Current-Page"]?.get(0))
        assertEquals("10", headers["X-Total-Pages"]?.get(0))
        assertEquals("100", headers["X-Total-Elements"]?.get(0))
        assertEquals("50", headers["X-Current-Element-Num"]?.get(0))
    }
}
