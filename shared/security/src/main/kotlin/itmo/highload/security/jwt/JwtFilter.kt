package itmo.highload.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

//@Profile("!disable-security")
//@Component
class JwtFilter(
    private val jwtUtils: JwtUtils,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = exchange.jwtAccessToken()
        if (token == null) {
            log.warn { "No access token found in request" }
            exchange.response.statusCode = UNAUTHORIZED
            return exchange.response.setComplete()
        }
        try {
            val username = jwtUtils.extractUsername(token)
            val role = jwtUtils.extractRole(token)
            log.info { "User $username with role $role is trying to access protected resource" }
            val authorities = listOf(SimpleGrantedAuthority(role.toString()))
            val auth = UsernamePasswordAuthenticationToken(
                jwtUtils.extractUsername(token),
                null,
                authorities
            )
            val context: Context = ReactiveSecurityContextHolder.withAuthentication(auth)
            return chain.filter(exchange).contextWrite(context)
        } catch (e: Exception) {
            log.warn { "Failed to validate access token: ${e.message}" }
            exchange.response.statusCode = UNAUTHORIZED
            return exchange.response.setComplete()
        }
    }

    companion object {
        fun ServerWebExchange.jwtAccessToken(): String? =
            request.headers.getFirst(AUTHORIZATION)?.let { it.ifEmpty { null } }?.substringAfter("Bearer ")

        private val log = KotlinLogging.logger {}
    }
}
