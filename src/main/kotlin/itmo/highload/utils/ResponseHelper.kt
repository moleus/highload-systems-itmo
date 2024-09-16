package itmo.highload.utils

import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity



class ResponseHelper {
    fun <T, D> createPaginatedResponse(
        entityPage: Page<T>,
        dataTransformer: (List<T>) -> List<D>,
        paginationHeadersCreator: PaginationHeadersCreator
    ): ResponseEntity<List<D>> {
        val dtoList: List<D> = dataTransformer(entityPage.content)
        val responseHeaders: HttpHeaders = paginationHeadersCreator.createPaginationHeaders(entityPage)

        return ResponseEntity.ok().headers(responseHeaders).body(dtoList)
    }
}
