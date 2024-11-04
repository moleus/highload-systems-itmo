@file:Suppress("MaxLineLength")

package itmo.highload.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import reactor.core.publisher.Mono
import javax.crypto.SecretKey


@Profile("!disable-security")
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class WebFluxSecurityConfig @Autowired constructor(
    @Value("\${jwt.secret.sign}") private val jwtAccessSecret: String
) {
    companion object {
        val EXCLUDED_PATHS = arrayOf(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/token",
            "/",
            "/static/**",
            "/index.html",
            "/favicon.ico",
            "/docs/**",
            "/docs.yaml",
            "/webjars/**",
            "/actuator/health",
            "/swagger-doc/v3/api-docs/**",
            "/swagger-doc/swagger-config/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/animal-api-service/v3/api-docs/**",
            "/adoption-api-service/v3/api-docs/**",
            "/transaction-api-service/v3/api-docs/**",
            "/auth-api-service/v3/api-docs/**",
            "/images-api-service/v3/api-docs/**",
        )
        val log = KotlinLogging.logger {}
    }

    @Bean
    fun configureSecurity(http: ServerHttpSecurity): SecurityWebFilterChain =
        http {
            httpBasic { disable() }
            csrf { disable() }
            authorizeExchange {
                @Suppress("SpreadOperator")
                authorize(pathMatchers(*EXCLUDED_PATHS), permitAll)
                authorize(anyExchange, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtDecoder = jwtDecoder(jwtAccessSecret = jwtAccessSecret)
                    jwtAuthenticationConverter = grantedAuthoritiesExtractor()
                }
            }
        }

    fun jwtDecoder(jwtAccessSecret: String): ReactiveJwtDecoder {
        val publicKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret))
        return NimbusReactiveJwtDecoder.withSecretKey(publicKey)
            .macAlgorithm(MacAlgorithm.HS512)
            .build()
    }

    fun grantedAuthoritiesExtractor(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesExtractor())
        return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
    }

    internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
        override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
            val authority: Any? = jwt.claims.getOrDefault("role", null)
            log.debug { "Authorities: $authority" }
            if (authority == null) {
                log.warn { "Authorities are not a list" }
                return emptyList()
            }
            return listOf(SimpleGrantedAuthority(authority.toString()))
        }
    }

}

@Profile("disable-security")
@Configuration
@EnableWebFluxSecurity
class AllowAllSecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .httpBasic { obj: ServerHttpSecurity.HttpBasicSpec -> obj.disable() }
        .csrf { obj -> obj.disable() }
        .authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }
        .build()
}
