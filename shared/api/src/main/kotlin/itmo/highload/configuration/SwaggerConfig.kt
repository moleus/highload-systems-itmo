package itmo.highload.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class CorsGlobalConfiguration : WebFluxConfigurer {
    private val allowedOrigins = arrayOf("http://localhost:*")
    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        @Suppress("SpreadOperator")
        corsRegistry.addMapping("/**")
            .allowedOrigins(*allowedOrigins)
            .allowedMethods("*")
    }
}

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI().addSecurityItem(
            SecurityRequirement().addList
                ("Bearer Authentication")
        )
            .components(Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
            .info(
                Info().title("REST API")
                    .description("Some custom description of API.")
                    .version("1.0")
            )
    }

    private fun createAPIKeyScheme(): SecurityScheme {
        return SecurityScheme().type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer")
    }
}
