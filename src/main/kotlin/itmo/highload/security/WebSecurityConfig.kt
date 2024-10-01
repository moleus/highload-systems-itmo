@file:Suppress("MaxLineLength")

package itmo.highload.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.web.SecurityFilterChain

@Profile("lab1")
@Configuration
@EnableWebSecurity
class DisableSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { requests -> requests.requestMatchers("/**").permitAll() }
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
        return http.build()
    }
}
