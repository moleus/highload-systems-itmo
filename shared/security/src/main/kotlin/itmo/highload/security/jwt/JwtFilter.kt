package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Profile("security")
@Component
class JwtFilter(
    private val jwtProvider: JwtUtils,
//    private val exceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        val jwtToken = getTokenFromRequest(request)
        if (jwtToken == null) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            jwtProvider.validateAccessToken(jwtToken)
            val claims: Claims = jwtProvider.getAccessClaims(jwtToken)
            val authorities = listOf(SimpleGrantedAuthority(claims["role"].toString()))
            val authentication = UsernamePasswordAuthenticationToken(
                claims.subject,
                null,
                authorities,
            )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: JwtException) {
            log.warn("Failed to validate access token: {}", e.message)
//            exceptionResolver.resolveException(request, response, null, e)
        }
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
