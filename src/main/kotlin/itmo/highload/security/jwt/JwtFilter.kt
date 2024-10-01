package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import itmo.highload.model.User
import itmo.highload.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Profile("security")
@Component
class JwtFilter(
    val jwtProvider: JwtProvider,
    val userService: UserService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwtToken = getTokenFromRequest(request)
        if (jwtToken != null) {
            try {
                jwtProvider.validateAccessToken(jwtToken)
                val claims: Claims = jwtProvider.getAccessClaims(jwtToken)
                val user: User = userService.getByLogin(claims.subject)
                val authentication = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: ExpiredJwtException) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "application/json"
                response.characterEncoding = "UTF-8"
                response.writer.write("""{"error": "${e.message}"}""")
            } catch (e: JwtException) {
                log.info("Failed to validate access token: {}", e.message)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val authPrefix = "Bearer "
        if (authHeader == null || !authHeader.startsWith(authPrefix)) {
            return null
        }
        return authHeader.substring(authPrefix.length)
    }
}
