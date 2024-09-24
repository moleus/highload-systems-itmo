package itmo.highload.utils

import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity

object PaginationResponseHelper {
    fun createInfinityScrollHeaders(entityPage: Page<*>): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-Current-Page", entityPage.number.toString())
        headers.add("X-Has-Next-Page", entityPage.hasNext().toString())

        return headers
    }

    fun createPaginationHeaders(entityPage: Page<*>): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-Current-Page", entityPage.number.toString())
        headers.add("X-Total-Pages", entityPage.totalPages.toString())
        headers.add("X-Total-Elements", entityPage.totalElements.toString())
        headers.add("X-Current-Element-Num", entityPage.numberOfElements.toString())

        return headers
    }

    fun <T> createPaginatedResponseWithHeaders(
        entityPage: Page<T>
    ): ResponseEntity<List<T>> {
        val responseHeaders: HttpHeaders = createPaginationHeaders(entityPage)
        return ResponseEntity.ok().headers(responseHeaders).body(entityPage.content)
    }
}
