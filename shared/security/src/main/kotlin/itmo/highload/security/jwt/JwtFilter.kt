package itmo.highload.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Profile("!disable-security")
@Component
class JwtFilter(
    private val jwtProvider: JwtUtils,
//    private val exceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

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
            KotlinLogging.logger {}.warn { "Failed to validate access token: ${e.message}" }
//            exceptionResolver.resolveException(request, response, null, e)
        }
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val authPrefix = "Bearer "
        KotlinLogging.logger {}.info { "Getting token from request. Auth header: $authHeader" }
        if (authHeader == null || !authHeader.startsWith(authPrefix)) {
            return null
        }
        return authHeader.substring(authPrefix.length)
    }
}
