package itmo.highload.utils

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity

object PaginationResponseHelper {
    private const val MAX_PAGE_SIZE = 50

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

    fun limitPageSize(pageable: Pageable): Pageable {
        return if (pageable.pageSize > MAX_PAGE_SIZE) {
            PageRequest.of(pageable.pageNumber, MAX_PAGE_SIZE, pageable.sort)
        } else {
            pageable
        }
    }

}
