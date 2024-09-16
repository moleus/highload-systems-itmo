package itmo.highload.security

import itmo.highload.security.jwt.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity, jwtFilter: JwtFilter?): SecurityFilterChain {
        http
                .httpBasic { obj: HttpBasicConfigurer<HttpSecurity> -> obj.disable() }
                .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
                .sessionManagement { management: SessionManagementConfigurer<HttpSecurity?> -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests {requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry ->
                    requests
                            .requestMatchers("/", "/bundle.js", "/index.html", "/api/auth/**", "/error").permitAll()
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                            .anyRequest().authenticated()
                }
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }
}
