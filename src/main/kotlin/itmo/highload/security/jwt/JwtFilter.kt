package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import itmo.highload.model.User
import itmo.highload.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
@RequiredArgsConstructor
class JwtFilter(val jwtProvider: JwtProvider,
                val userService: UserService
) : OncePerRequestFilter() {


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        val jwtToken = getTokenFromRequest(request)
        if (jwtToken != null && jwtProvider.validateAccessToken(jwtToken)) {
            val claims: Claims = jwtProvider.getAccessClaims(jwtToken)
            val user: User = userService.getByLogin(claims.subject)
            val authentication = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.authorities)
            SecurityContextHolder.getContext().authentication = authentication
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
